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
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.Thread.sleep
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.*

class AgentInstallerImplTest {

    private var testDownloadDir = Files.createTempDirectory("drill").toFile()

    @AfterTest
    fun clearDownloadDir() {
        testDownloadDir.deleteRecursively()
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
        val agentInstaller = AgentInstallerImpl().also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, testVersion, testOsPreset)

        assertNotNull(result)
        assertEquals(testDownloadUrl, result.url)
        assertEquals(testFilename, result.filename)
    }

    @Test
    fun `given correct version matching, getDownloadUrl should return download url`() = runBlocking {
        val testRepository = "test/repository"
        val testVersion = "1.0.9"
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
        val agentInstaller = AgentInstallerImpl().also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, "1.0.+", testOsPreset)

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
        val agentInstaller = AgentInstallerImpl().also { it.httpClient = mockHttpClient }

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
        val agentInstaller = AgentInstallerImpl().also { it.httpClient = mockHttpClient }

        val result = agentInstaller.getDownloadUrl(testRepository, testVersion, testOsPreset)

        assertNull(result)
    }


    @Test
    fun `given valid download URL, download should save the file`() = runBlocking {
        val testDownloadUrl = FileUrl("https://example.com/file.txt", "file.txt")

        val mockHttpClient = mockHttpClient(
            "/file.txt" shouldRespond """
             Some text content
            """.trimIndent()
        )
        val agentInstaller = AgentInstallerImpl().also { it.httpClient = mockHttpClient }

        val result = agentInstaller.download(testDownloadUrl, testDownloadDir)
        sleep(1000)
        assertEquals(testDownloadUrl.filename, result.name)
        assertEquals("Some text content", result.readText())
    }

    @Test
    fun `given valid zip file, unzip should extract files`() {
        val unzippedDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }
        val zipFile = File(unzippedDir, "agent.zip")
        listOf("file1.txt", "file2.so", "file3.jar").createZip(zipFile)
        val agentInstaller = AgentInstallerImpl()

        val result = agentInstaller.unzip(zipFile)

        assertEquals(File(unzippedDir, zipFile.nameWithoutExtension), result)
    }

    @Test
    fun `given directory with agent file, findAgentFile should return the file`() {
        val testAgentFile = "file2.so"
        val unzippedDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }
        val dirInsideUnzippedDir = Directory(unzippedDir, "subdir").also { it.mkdir() }
        listOf("file1.txt", testAgentFile, "file3.jar").createFiles(dirInsideUnzippedDir)
        val agentInstaller = AgentInstallerImpl()

        val result = agentInstaller.findAgentFile(unzippedDir, "so")

        assertEquals(File(dirInsideUnzippedDir, testAgentFile), result)
    }

    @Test
    fun `given directory without agent file, findAgentFile should return null`() {
        val unzippedDir = Directory(testDownloadDir, "unzipped").also { it.mkdir() }
        val dirInsideUnzippedDir = Directory(unzippedDir, "subdir").also { it.mkdir() }
        listOf("file1.txt", "file2.so", "file3.jar").createFiles(dirInsideUnzippedDir)
        val agentInstaller = AgentInstallerImpl()

        val result = agentInstaller.findAgentFile(unzippedDir, "dll")

        assertNull(result)
    }


}

infix fun String.shouldRespond(content: String) = Pair(this, content)

fun mockHttpClient(vararg requestHandlers: Pair<String, String>) = HttpClient(MockEngine { request ->
    requestHandlers
        .also { println(request.url.encodedPath) }
        .find { request.url.encodedPath.contains(it.first, ignoreCase = true) }
        ?.run { respondOk(this.second) }
        ?: respondError(HttpStatusCode.NotFound, "There is no handler for request: $request")
}) {
    expectSuccess = true
}

fun List<String>.createFiles(dir: Directory) {
    forEach { file ->
        File(dir, file).createNewFile()
    }
}

fun List<String>.createZip(zipFile: File) {
    ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
        for (fileName in this) {
            val entry = ZipEntry(fileName)
            zos.putNextEntry(entry)
            zos.closeEntry()
        }
    }
}
