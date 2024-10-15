/**
 * Copyright 2020 - 2022 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.drill.integration.common.agent.impl

import com.epam.drill.integration.common.agent.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.zip.ZipFile
import kotlin.io.use

private const val GITHUB_API_URL = "https://api.github.com"
private const val GITHUB_USER_TOKEN = "GH_USER_TOKEN"

class AgentInstallerImpl(
    private val agentCache: AgentCache
) : AgentInstaller {
    private val json = Json { ignoreUnknownKeys = true }
    private val token: String? = System.getenv(GITHUB_USER_TOKEN)

    var httpClient: HttpClient = HttpClient(CIO) {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    override suspend fun getDownloadUrl(githubRepository: String, version: String, osPreset: String): FileUrl? {
        val releasesUrl = "$GITHUB_API_URL/repos/$githubRepository/releases"

        return httpClient.get<HttpResponse>(releasesUrl) {
            parameter("prerelease", true)
            headers {
                token?.let { append(HttpHeaders.Authorization, "Bearer $it") }
            }
        }.let { response ->
            json.parseToJsonElement(response.readText())
        }.jsonArray.firstOrNull { release ->
            val tag = release.jsonObject["tag_name"]?.jsonPrimitive?.content
            tag?.removePrefix("v") == version
        }?.jsonObject?.get("assets")?.jsonArray?.firstOrNull { asset ->
            val fileName = asset.jsonObject["name"]?.jsonPrimitive?.content
            fileName
                ?.contains(osPreset)
                ?: false
        }?.let { asset ->
            val fileName = asset.jsonObject["name"]?.jsonPrimitive?.content
                ?: throw IllegalStateException("Can't get file name from $asset")
            val url = asset.jsonObject["browser_download_url"]?.jsonPrimitive?.content
                ?: throw IllegalStateException("Can't get url from $asset")
            FileUrl(url, fileName)
        }
    }

    override suspend fun downloadByVersion(githubRepository: String, agentName: String, version: String): File = run {
        getDownloadUrl(githubRepository, version, currentOsPreset)
    }?.let { (url, _) ->
        agentCache.get(agentName, version, currentOsPreset) { filename, downloadDir ->
            downloadFile(FileUrl(url, filename), downloadDir)
        }
    } ?: throw IllegalStateException("Agent version $version not found")


    override suspend fun downloadByUrl(downloadUrl: String, agentName: String): File =
        agentCache.get(agentName, downloadUrl.hashCode().toString(), currentOsPreset) { filename, downloadDir ->
            downloadFile(FileUrl(downloadUrl, filename), downloadDir)
        }

    override fun unzip(zipFile: File, destinationDir: Directory): Directory {
        if (!zipFile.exists()) {
            throw IllegalStateException("File $zipFile doesn't exist")
        }
        val unzippedDir = Directory(destinationDir, zipFile.nameWithoutExtension)
        if (!unzippedDir.exists()) {
            unzippedDir.mkdirs()
        }
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) {
                    File(unzippedDir, entry.name).mkdirs()
                } else {
                    zip.getInputStream(entry).use { input ->
                        File(unzippedDir, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
        return unzippedDir
    }

    override fun findAgentFile(unzippedDir: Directory, fileExtension: String): File? {
        return findFile(unzippedDir, fileExtension)
    }

    private suspend fun downloadFile(downloadUrl: FileUrl, downloadDir: Directory): File {
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        val file = File(downloadDir, downloadUrl.filename)
        if (!file.exists()) {
            val response: HttpResponse = httpClient.get(downloadUrl.url) {
                headers {
                    token?.let { append(HttpHeaders.Authorization, "Bearer $it") }
                }
            }
            val channel: ByteReadChannel = response.receive()

            file.outputStream().use { fileStream ->
                val buffer = ByteArray(4096)
                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer)
                    if (bytesRead > 0) {
                        fileStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
        return file
    }

    private fun findFile(directory: Directory, fileExtension: String): File? {
        val files = directory.listFiles() ?: return null

        for (file in files) {
            if (file.isDirectory) {
                val result = findFile(file, fileExtension)
                if (result != null) {
                    return result
                }
            } else if (file.extension == fileExtension) {
                return file
            }
        }

        return null
    }
}