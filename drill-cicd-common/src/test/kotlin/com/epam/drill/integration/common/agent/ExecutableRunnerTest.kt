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

import com.epam.drill.integration.common.agent.config.AppArchiveScannerConfiguration
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.*
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExecutableRunnerTest {
    private val distDir = Files.createTempDirectory("dist").toFile()
    private val archive = File.createTempFile("archive", ".zip")
    private val agentInstaller = mock<AgentInstaller>()

    private val mockScannerRunner: suspend (List<String>, File, suspend (String) -> Unit) -> Int = { _, _, output ->
        output("mocked output line 1")
        output("mocked output line 2")
        42
    }

    private val executableRunner = ExecutableRunner(agentInstaller, mockScannerRunner)

    @AfterTest
    fun cleanup() {
        distDir.deleteRecursively()
        archive.delete()
    }

    @Test
    fun `runScan returns mocked exit code and outputs`(): Unit = runBlocking {
        val zipFile = File(distDir, "scanner.zip").apply { createNewFile() }
        val executable = File(distDir, "scanner.exe").apply { createNewFile(); setExecutable(true) }

        val config = AppArchiveScannerConfiguration().let {
            it.zipPath = zipFile
            it
        }

        whenever(agentInstaller.unzip(zipFile, distDir)).thenReturn(object : File(distDir, "") {
            override fun listFiles() = arrayOf(executable)
        })

        val outputLines = mutableListOf<String>()

        val exitCode = executableRunner.runScan(config, distDir, archive) { line ->
            outputLines.add(line)
        }

        assertEquals(42, exitCode)
        assertEquals(listOf("mocked output line 1", "mocked output line 2"), outputLines)
        verify(agentInstaller).unzip(zipFile, distDir)
    }

    @Test
    fun `runScan throws IllegalStateException if no parameters`(): Unit = runBlocking {
        val config = AppArchiveScannerConfiguration()

        assertFailsWith<IllegalStateException> {
            executableRunner.runScan(config, distDir, archive) {}
        }
    }

    @Test
    fun `runScan downloads by downloadUrl if zipPath is null`(): Unit = runBlocking {
        val url = "http://example.com/scanner.zip"
        val zipFile = File(distDir, "downloaded.zip").apply { createNewFile() }
        val executable = File(distDir, "scanner.exe").apply { createNewFile(); setExecutable(true) }

        val config = AppArchiveScannerConfiguration().let {
            it.downloadUrl = url
            it
        }

        wheneverBlocking(agentInstaller) { downloadByUrl(url, config.agentName) }.thenReturn(zipFile)
        whenever(agentInstaller.unzip(zipFile, distDir)).thenReturn(object : File(distDir, "") {
            override fun listFiles() = arrayOf(executable)
        })

        val outputLines = mutableListOf<String>()
        val exitCode = executableRunner.runScan(config, distDir, archive) { line -> outputLines.add(line) }

        assertEquals(42, exitCode)
        verifyBlocking(agentInstaller) { downloadByUrl(url, config.agentName) }
        verify(agentInstaller).unzip(zipFile, distDir)
    }

    @Test
    fun `runScan downloads by version if zipPath and downloadUrl are null`(): Unit = runBlocking {
        val version = "1.2.3"
        val zipFile = File(distDir, "versioned.zip").apply { createNewFile() }
        val executable = File(distDir, "scanner.exe").apply { createNewFile(); setExecutable(true) }

        val config = AppArchiveScannerConfiguration().let {
            it.version = version
            it
        }

        wheneverBlocking(agentInstaller) {
            downloadByVersion(config.githubRepository, config.agentName, version)
        }.thenReturn(zipFile)

        whenever(agentInstaller.unzip(zipFile, distDir)).thenReturn(object : File(distDir, "") {
            override fun listFiles() = arrayOf(executable)
        })

        val outputLines = mutableListOf<String>()
        val exitCode = executableRunner.runScan(config, distDir, archive) { line -> outputLines.add(line) }

        assertEquals(42, exitCode)
        verifyBlocking(agentInstaller) { downloadByVersion(config.githubRepository, config.agentName, version) }
        verify(agentInstaller).unzip(zipFile, distDir)
    }

    @Test
    fun `runScan priority is zipPath over downloadUrl over version`() = runBlocking {
        val zipFile = File(distDir, "zipPath.zip").apply { createNewFile() }
        val executable = File(distDir, "scanner.exe").apply { createNewFile(); setExecutable(true) }

        val config = AppArchiveScannerConfiguration().let {
            it.zipPath = zipFile
            it.downloadUrl = "http://example.com/should-not-be-used.zip"
            it.version = "9.9.9"
            it
        }

        wheneverBlocking(agentInstaller) { downloadByUrl(any(), any()) }.thenThrow(IllegalStateException("Should not call downloadByUrl"))
        wheneverBlocking(agentInstaller) { downloadByVersion(any(), any(), any()) }.thenThrow(IllegalStateException("Should not call downloadByVersion"))
        whenever(agentInstaller.unzip(zipFile, distDir)).thenReturn(object : File(distDir, "") {
            override fun listFiles() = arrayOf(executable)
        })

        val outputLines = mutableListOf<String>()
        val exitCode = executableRunner.runScan(config, distDir, archive) { line -> outputLines.add(line) }

        assertEquals(42, exitCode)
        verify(agentInstaller).unzip(zipFile, distDir)
        verifyBlocking(agentInstaller, times(0)) { downloadByUrl(any(), any()) }
        verifyBlocking(agentInstaller, times(0)) { downloadByVersion(any(), any(), any()) }
    }
}

private fun <M, T> wheneverBlocking(mock: M, methodCall: suspend M.() -> T) =
    runBlocking { whenever(mock.methodCall()) }

private suspend fun <M> verifyBlocking(mock: M, methodCall: suspend M.() -> Unit) =
    runBlocking { verify(mock).methodCall() }
