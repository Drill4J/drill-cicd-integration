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
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.zip.ZipFile
import kotlin.io.use

private const val GITHUB_API_URL = "https://api.github.com"

class AgentInstallerImpl : AgentInstaller {
    private val json = Json { ignoreUnknownKeys = true }

    var httpClient: HttpClient = HttpClient(CIO) {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    override suspend fun getDownloadUrl(githubRepository: String, versionMatching: String, osPreset: String): FileUrl? {
        val releasesUrl = "$GITHUB_API_URL/repos/$githubRepository/releases"

        return httpClient.get<HttpResponse>(releasesUrl) {
            parameter("prerelease", true)
        }.let { response ->
            json.parseToJsonElement(response.readText())
        }.jsonArray.firstOrNull { release ->
            val tag = release.jsonObject["tag_name"]?.jsonPrimitive?.content
            tag
                ?.removePrefix("v")
                ?.matches(versionMatching.toRegex())
                ?: false
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

    override suspend fun download(downloadUrl: FileUrl, downloadDir: Directory): File {
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        val file = File(downloadDir, downloadUrl.filename)
        if (!file.exists()) {
            val response: HttpResponse = httpClient.get(downloadUrl.url)
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

    override fun unzip(zipFile: File): Directory {
        val destDir = Directory(zipFile.parentFile, zipFile.nameWithoutExtension)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                if (entry.isDirectory) {
                    File(destDir, entry.name).mkdirs()
                } else {
                    zip.getInputStream(entry).use { input ->
                        File(destDir, entry.name).outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
        return destDir
    }

    override fun findAgentFile(unzippedDir: Directory, fileExtension: String): File? {
        return findFile(unzippedDir, fileExtension)
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