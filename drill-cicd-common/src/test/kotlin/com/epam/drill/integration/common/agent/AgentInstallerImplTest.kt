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
    fun `given valid download URL, downloadAndUnzip should download and unzip file`() = runBlocking {
        val downloadUrl = "https://example.com/agent.zip"
        val agentName = "agent"
        val version = "1.0.0"
        val agentFilenames = listOf("file1.txt", "file2.so", "file3.jar")
        val zipFile = File(testZipDir, "agent.zip").apply { zipFiles(agentFilenames) }
        val mockHttpClient = mockHttpClient(
            "/agent.zip" shouldRespondBytes zipFile.readBytes()
        )
        val agentInstaller = AgentInstallerImpl(agentCache).also { it.httpClient = mockHttpClient }

        val agentFiles = agentInstaller.downloadAndUnzip(downloadUrl, agentName, version, testInstallationDir)

        assertTrue(agentFiles.containsFiles(agentFilenames))
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
        listOf("file1.txt", testAgentFile, "file3.jar").createFiles(dirInsideUnzippedDir)
        val agentInstaller = AgentInstallerImpl(agentCache)

        val result = agentInstaller.findAgentFile(unzippedDir, "so")

        assertEquals(File(dirInsideUnzippedDir, testAgentFile), result)
    }

    @Test
    fun `given directory without agent file, findAgentFile should return null`() {
        val unzippedDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }
        val dirInsideUnzippedDir = Directory(unzippedDir, "subdir").also { it.mkdir() }
        listOf("file1.txt", "file2.so", "file3.jar").createFiles(dirInsideUnzippedDir)
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

private fun mockHttpClient(vararg requestHandlers: Pair<String, ByteArray>) = HttpClient(MockEngine { request ->
    requestHandlers
        .also { println(request.url.encodedPath) }
        .find { request.url.encodedPath.contains(it.first, ignoreCase = true) }
        ?.run { respond(this.second) }
        ?: respondError(HttpStatusCode.NotFound, "There is no handler for request: $request")
}) {
    expectSuccess = true
}

private fun List<String>.createFiles(dir: Directory) {
    forEach { file ->
        File(dir, file).createNewFile()
    }
}


private fun File.containsFiles(filenames: List<String>) = filenames.all { File(this, it).exists() }

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