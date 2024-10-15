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

import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.*
import org.mockito.stubbing.OngoingStubbing
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AgentRunnerTest {
    private val cacheDir = Files.createTempDirectory("drill-cache").toFile()
    private val zipDir = Files.createTempDirectory("drill-zip").toFile()
    private val installationDir = Files.createTempDirectory("drill-installation").toFile()
    private val agentInstaller = mock<AgentInstaller>()
    private val agentRunner = AgentRunner(agentInstaller)


    @AfterTest
    fun clearTempDirs() {
        cacheDir.deleteRecursively()
        zipDir.deleteRecursively()
    }


    @Test
    fun `given zipPath, getJvmOptionsToRun should unzip agent by zipPath and return agentpath and other expected options`() {
        val agentZipFile = File(zipDir, "agent.zip")

        val configuration = TestAgentConfiguration().apply {
            zipPath = agentZipFile
            additionalParams = mapOf("arg1" to "value1")
        }
        val agentDir = Directory(installationDir, "agent")
        val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
        whenever(agentInstaller.unzip(any(), any())).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(any(), any())).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(installationDir, configuration)
        }

        assertEquals("-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1", result.first())
        verify(agentInstaller).unzip(agentZipFile, installationDir)
        verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)
    }

    @Test
    fun `given downloadUrl, getJvmOptionsToRun should download agent by url and return agentpath and other expected options`() {
        val agentDownloadUrl = "http://example.com/agent.zip"
        val configuration = TestAgentConfiguration().apply {
            downloadUrl = agentDownloadUrl
            additionalParams = mapOf("arg1" to "value1")
        }
        val agentZipFile = File(cacheDir, "agent.zip")
        val agentDir = Directory(installationDir, "agent")
        val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
        wheneverBlocking(agentInstaller) { downloadByUrl(any(), any()) }.thenReturn(agentZipFile)
        whenever(agentInstaller.unzip(any(), any())).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(installationDir, configuration)
        }

        assertEquals("-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1", result.first())
        verifyBlocking(agentInstaller) { downloadByUrl(eq(agentDownloadUrl), eq(configuration.agentName)) }
        verify(agentInstaller).unzip(agentZipFile, installationDir)
        verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)
    }

    @Test
    fun `given version, getJvmOptionsToRun should download agent by version and return agentpath and other expected options`() {
        val agentVersion = "1.0.0"
        val configuration = TestAgentConfiguration().apply {
            version = agentVersion
            additionalParams = mapOf("arg1" to "value1")
        }
        val agentZipFile = File(cacheDir, "agent.zip")
        val agentDir = Directory(installationDir, "agent")
        val agentLibFile = File(agentDir, "agent.so")
        val downloadUrl = "http://example.com/agent.zip"
        wheneverBlocking(agentInstaller) { getDownloadUrl(any(), any(), any()) }.thenReturn(
            FileUrl(filename = "agent.zip", url = downloadUrl)
        )
        wheneverBlocking(agentInstaller) { downloadByVersion(any(), any(), any()) }.thenReturn(agentZipFile)
        whenever(agentInstaller.unzip(any(), any())).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(any(), eq(currentOsLibExt))).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(installationDir, configuration)
        }

        assertEquals(
            "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
            result.first()
        )
        verifyBlocking(agentInstaller) { downloadByVersion(eq(configuration.githubRepository), eq(configuration.agentName), eq(agentVersion)) }
        verify(agentInstaller).findAgentFile(any(), eq(currentOsLibExt))
    }

    @Test
    fun `given configuration without required fields, getJvmOptionsToRun should throw IllegalStateException`() {
        val distDir = mock<Directory>()
        val configuration = TestAgentConfiguration().apply {
            zipPath = null
            downloadUrl = null
            version = null
        }

        assertFailsWith<IllegalStateException> {
            runBlocking {
                agentRunner.getJvmOptionsToRun(distDir, configuration)
            }
        }
    }

}

private fun anyFile() = File("agent.zip")

private fun anyContent() = byteArrayOf(1, 2, 3)

private fun <M, T> wheneverBlocking(mock: M, methodCall: suspend M.() -> T): OngoingStubbing<T> {
    return runBlocking { whenever(mock.methodCall()) }
}