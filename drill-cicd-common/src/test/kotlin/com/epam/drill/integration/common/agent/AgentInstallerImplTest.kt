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
package com.epam.drill.integration.common.agent

import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.*

class AgentInstallerImplTest {
    private val testDownloadDir = Files.createTempDirectory("drill-download").toFile()
    private val testInstallationDir = Files.createTempDirectory("drill-installation").toFile()
    private val testZipDir = Files.createTempDirectory("drill-zip").toFile()
    private val agentCache = AgentCacheStub(testDownloadDir)

    @AfterTest
    fun clearTempDirs() {
        testDownloadDir.deleteRecursively()
        testInstallationDir.deleteRecursively()
        testZipDir.deleteRecursively()
    }

    @Test
    fun `given existing version and OS preset, getDownloadUrl should return download url`() = runBlocking {
        val testRepository = "test/repository"
        val testVersion = "1.0.0"
        val testOsPreset = "linuxX64"
        val testFilename = "agent-$testOsPreset-$testVersion.zip"
        val testDownloadUrl = "http://example.com/$testRepository/releases/download/v$testVersion/$testFilename"
        val mockHttpClient = mockHttpClient()
        val agentInstaller = AgentInstallerImpl(agentCache = agentCache, githubUrl = "http://example.com")
            .also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, testVersion, testOsPreset)

        assertNotNull(result)
        assertEquals(testDownloadUrl, result.url)
        assertEquals(testFilename, result.filename)
    }


    @Test
    fun `given valid download URL, downloadByUrl should download file`() = runBlocking {
        val downloadUrl = "https://example.com/agent.zip"
        val agentName = "agent"
        val agentFileContent = "test agent content"
        val agentZipFile = File(testDownloadDir, "agent.zip").apply {
            writeContent(agentFileContent)
        }
        val mockHttpClient = mockHttpClient(
            "/agent.zip" shouldRespondBytes agentZipFile.readBytes()
        )
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        val result = agentInstaller.downloadByUrl(downloadUrl, agentName)

        assertEquals(agentZipFile.readBytes().toList(), result.readBytes().toList())
    }

    @Test
    fun `given valid version, downloadByVersion should get URL and download file`() = runBlocking {
        val agentVersion = "1.0.0"
        val agentName = "agent"
        val agentRepository = "test/repository"
        val agentFilename = "agent-$currentOsPreset-$agentVersion.zip"
        val agentDownloadUrl = "/download/$agentFilename"
        val agentFileContent = "test agent content"
        val agentZipFile = File(testDownloadDir, agentFilename).apply {
            writeContent(agentFileContent)
        }
        val mockHttpClient = mockHttpClient(
            "/repos/$agentRepository/releases" shouldRespond """
                [
                    {
                        "tag_name": "v$agentVersion",
                        "assets": [
                            {
                                "name": "$agentFilename",
                                "browser_download_url": "$agentDownloadUrl"
                            }
                        ]
                    }
                ]
                """.trimIndent(),
            agentDownloadUrl shouldRespondBytes agentZipFile.readBytes()
        )
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        val result = agentInstaller.downloadByVersion(agentRepository, agentName, agentVersion)

        assertEquals(agentZipFile.readBytes().toList(), result.readBytes().toList())
    }

    @Test
    fun `given zipPath, installAgent should unzip agent`() {
        val agentFilenames = listOf("file1.txt", "file2.so", "file3.jar")
        val zipFile = File(testZipDir, "agent-test.zip").apply { zipFiles(agentFilenames) }
        val configuration = AgentConfiguration().apply {
            zipPath = zipFile
        }
        val agentInstaller = AgentInstallerImpl(agentCache)

        runBlocking {
            agentInstaller.installAgent(testInstallationDir, configuration)
        }.also { agentDir ->
            assertEquals(File(testInstallationDir, "agent-test").absolutePath, agentDir.absolutePath)
            agentFilenames.forEach { fileName ->
                assertTrue(File(agentDir, fileName).exists())
            }
        }
    }

    @Test
    fun `given downloadUrl, installAgent should download agent by url and unzip it`() {
        val testDownloadUrl = "https://example.com/agent.zip"
        val agentFilenames = listOf("file1.txt", "file2.so", "file3.jar")
        val zipFile = File(testZipDir, "agent-test.zip").apply { zipFiles(agentFilenames) }
        val mockHttpClient = mockHttpClient(
            "/agent.zip" shouldRespondBytes zipFile.readBytes()
        )
        val configuration = AgentConfiguration().apply {
            downloadUrl = testDownloadUrl
        }
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        runBlocking {
            agentInstaller.installAgent(testInstallationDir, configuration)
        }.also { agentDir ->
            val expectedAgentDirName = "${configuration.agentName}-$currentOsPreset-${testDownloadUrl.hashCode()}"
            assertEquals(File(testInstallationDir, expectedAgentDirName), agentDir)
            agentFilenames.forEach { fileName ->
                assertTrue(File(agentDir, fileName).exists())
            }
        }
    }

    @Test
    fun `given version, installAgent should download agent by version and unzip it`() {
        val agentVersion = "1.0.0"
        val agentRepository = "test/repository"
        val agentName = "agent-test"
        val agentFilename = "agent-$currentOsPreset-$agentVersion.zip"
        val repoApiUrl = "http://example.com"
        val agentFilenames = listOf("file1.txt", "file2.so", "file3.jar")
        val zipFile = File(testZipDir, "agent-test.zip").apply { zipFiles(agentFilenames) }
        val mockHttpClient = mockHttpClient(
            "/$agentRepository/releases/download/v$agentVersion/$agentFilename" shouldRespondBytes zipFile.readBytes()
        )
        val configuration = AgentConfiguration().apply {
            this.version = agentVersion
            this.githubRepository = agentRepository
            this.agentName = agentName
        }
        val agentInstaller = AgentInstallerImpl(agentCache, repoApiUrl)
            .also { it.httpClient = mockHttpClient }

        runBlocking {
            agentInstaller.installAgent(testInstallationDir, configuration)
        }.also { agentDir ->
            val expectedAgentDirName = "${configuration.agentName}-$currentOsPreset-$agentVersion"
            assertEquals(File(testInstallationDir, expectedAgentDirName), agentDir)
            agentFilenames.forEach { fileName ->
                assertTrue(File(agentDir, fileName).exists())
            }
        }
    }

    @Test
    fun `given configuration without required fields, installAgent should throw IllegalStateException`() {
        val configuration = AgentConfiguration().apply {
            zipPath = null
            downloadUrl = null
            version = null
        }
        val agentInstaller = AgentInstallerImpl(agentCache)
        // Simulate a case where no download URL or zip path is provided

        assertFailsWith<IllegalStateException> {
            runBlocking {
                agentInstaller.installAgent(testInstallationDir, configuration)
            }
        }
    }

}

infix fun String.shouldRespond(content: String) = Pair(this, content.toByteArray())
infix fun String.shouldRespondBytes(content: ByteArray) = Pair(this, content)

private fun File.zipFiles(filenames: List<String>) {
    if (!this.exists()) {
        this.createNewFile()
    }
    ZipOutputStream(FileOutputStream(this)).use { zos ->
        for (fileName in filenames) {
            val entry = ZipEntry(fileName)
            zos.putNextEntry(entry)
            zos.closeEntry()
        }
    }
}

private fun File.writeContent(content: String) {
    if (!this.exists())
        this.createNewFile()
    this.outputStream().use { it.write(content.toByteArray()) }
}

private fun mockHttpClient(vararg requestHandlers: Pair<String, ByteArray>) = HttpClient(MockEngine { request ->
    requestHandlers
        .also { println(request.url.encodedPath) }
        .find { request.url.encodedPath.contains(it.first, ignoreCase = true) }
        ?.run { respond(this.second) }
        ?: respondError(HttpStatusCode.NotFound, "There is no handler for request: $request")
}) {
    expectSuccess = true
}

private fun Directory.createFiles(filenames: List<String>) {
    filenames.forEach { file ->
        File(this, file).createNewFile()
    }
}

private fun Directory.containsFiles(filenames: List<String>) = filenames.all { File(this, it).exists() }

private class AgentCacheStub(private val tempDir: Directory) : AgentCache {
    override fun clearAll() {
    }

    override fun clear(agentName: String, version: String, preset: String) {
    }

    override suspend fun get(
        agentName: String,
        version: String,
        preset: String,
        download: suspend (filename: String, downloadDir: Directory) -> Unit
    ): File {
        val filename = "$agentName-$preset-$version.zip"
        val file = File(tempDir, filename)
        download(filename, tempDir)
        return file
    }
}