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
import com.epam.drill.integration.common.agent.config.AgentConfiguration
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
import mu.KotlinLogging
import java.io.File
import kotlin.io.use

private const val GITHUB_URL = "https://github.com"
private const val GITHUB_API_URL = "https://api.github.com"
private const val GITHUB_USER_TOKEN = "GH_USER_TOKEN"

class AgentInstallerImpl(
    private val agentCache: AgentCache,
    private val githubUrl: String = GITHUB_URL,
    repoTokenName: String = GITHUB_USER_TOKEN,
) : AgentInstaller {
    private val json = Json { ignoreUnknownKeys = true }
    private val token: String? = System.getenv(repoTokenName)
    private val logger = KotlinLogging.logger {}

    var httpClient: HttpClient = HttpClient(CIO) {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun getDownloadUrl(githubRepository: String, version: String, osPreset: String): FileUrl? {
        val fileName = "agent-$osPreset-$version.zip"
        val downloadUrl = "$githubUrl/$githubRepository/releases/download/v$version/$fileName"
        return FileUrl(downloadUrl, fileName)
    }

    suspend fun downloadByVersion(githubRepository: String, agentName: String, version: String): File =
        agentCache.get(agentName, version, currentOsPreset) { filename, downloadDir ->
            run {
                getDownloadUrl(githubRepository, version, currentOsPreset)
            }?.let { (url, _) ->
                downloadFile(FileUrl(url, filename), downloadDir)
            }?.also { file ->
                logger.info { "Agent ${file.name} has been downloaded" }
            } ?: throw IllegalStateException("Can't get download url for $agentName")
        }


    suspend fun downloadByUrl(downloadUrl: String, agentName: String): File =
        agentCache.get(agentName, downloadUrl.hashCode().toString(), currentOsPreset) { filename, downloadDir ->
            downloadFile(FileUrl(downloadUrl, filename), downloadDir)
        }

    override suspend fun downloadAgent(
        configuration: AgentConfiguration
    ): File {
        configuration.zipPath?.takeIf { it.exists() }?.let {
            return it
        }

        configuration.downloadUrl?.takeIf { it.isNotBlank() }?.let {
            return downloadByUrl(it, configuration.agentName)
        }

        configuration.version?.takeIf { it.isNotBlank() }?.let {
            return downloadByVersion(
                configuration.githubRepository,
                configuration.agentName,
                it
            )
        }

        throw IllegalStateException(
            "Could not download or find ${configuration.agentName} zip. " +
                    "Specify either of parameters: version, downloadUrl, zipPath"
        )
    }

    override suspend fun installAgent(
        distDir: Directory,
        configuration: AgentConfiguration
    ): Directory = unzip(downloadAgent(configuration), distDir)

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
}