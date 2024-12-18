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
        val testFilename = "$testOsPreset-$testVersion.zip"
        val testDownloadUrl = "https://example.com/$testFilename"
        val mockHttpClient = mockHttpClient(
            "/repos/$testRepository/releases" shouldRespond """
                [
                    {
                        "tag_name": "v$testVersion",
                        "assets": [
                            {
                                "name": "$testFilename",
                                "browser_download_url": "$testDownloadUrl"
                            }
                        ]
                    }
                ]
                """.trimIndent()
        )
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, testVersion, testOsPreset)

        assertNotNull(result)
        assertEquals(testDownloadUrl, result.url)
        assertEquals(testFilename, result.filename)
    }

    @Test
    fun `given non-existing version, getDownloadUrl should return null`() = runBlocking {
        val testRepository = "test/repository"
        val testVersion = "2.0.0"
        val testOsPreset = "linuxX64"

        val mockHttpClient = mockHttpClient(
            "/repos/$testRepository/releases" shouldRespond """
             [
                {
                    "tag_name": "v1.0.0",
                    "assets": [
                        {
                            "name": "$testOsPreset-2.0.0.zip",
                            "browser_download_url": "https://example.com/$testOsPreset-1.0.0.zip"
                        }
                    ]
                }
            ]
            """.trimIndent()
        )
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, testVersion, testOsPreset)

        assertNull(result)
    }

    @Test
    fun `given non-existing OS preset, getDownloadUrl should return null`() = runBlocking {
        val testRepository = "test/repository"
        val testVersion = "1.0.0"
        val testOsPreset = "mingwX64"

        val mockHttpClient = mockHttpClient(
            "/repos/$testRepository/releases" shouldRespond """
             [
                {
                    "tag_name": "v$testVersion",
                    "assets": [
                        {
                            "name": "linuxX64-$testVersion.zip",
                            "browser_download_url": "https://example.com/linuxX64-$testVersion.zip"
                        }
                    ]
                }
            ]
            """.trimIndent()
        )
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, testVersion, testOsPreset)

        assertNull(result)
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
    fun `given valid zip file and destination directory, unzip should extract files to destination directory`() {
        val agentFilenames = listOf("file1.txt", "file2.so", "file3.jar")
        val zipFile = File(testZipDir, "agent.zip").apply { zipFiles(agentFilenames) }
        val agentInstaller = AgentInstallerImpl(agentCache)
        val destinationDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }

        val result = agentInstaller.unzip(zipFile, destinationDir)

        assertTrue(result.containsFiles(agentFilenames))
    }

    @Test
    fun `given directory with agent file, findAgentFile should return the file`() {
        val testAgentFile = "file2.so"
        val unzippedDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }
        val dirInsideUnzippedDir = Directory(unzippedDir, "subdir").also { it.mkdir() }
        dirInsideUnzippedDir.createFiles(listOf("file1.txt", testAgentFile, "file3.jar"))
        val agentInstaller = AgentInstallerImpl(agentCache)

        val result = agentInstaller.findAgentFile(unzippedDir, "so")

        assertEquals(File(dirInsideUnzippedDir, testAgentFile), result)
    }

    @Test
    fun `given directory without agent file, findAgentFile should return null`() {
        val unzippedDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }
        val dirInsideUnzippedDir = Directory(unzippedDir, "subdir").also { it.mkdir() }
        dirInsideUnzippedDir.createFiles(listOf("file1.txt", "file2.so", "file3.jar"))
        val agentInstaller = AgentInstallerImpl(agentCache)

        val result = agentInstaller.findAgentFile(unzippedDir, "dll")

        assertNull(result)
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