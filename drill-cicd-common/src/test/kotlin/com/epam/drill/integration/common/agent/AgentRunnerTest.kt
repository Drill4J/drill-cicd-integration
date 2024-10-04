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
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import org.mockito.stubbing.OngoingStubbing
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AgentRunnerTest {
    @field:TempDir
    lateinit var tempFolder: File

    private val agentInstaller = mock<AgentInstaller>()
    private lateinit var agentCache: AgentCache
    private lateinit var agentRunner: AgentRunner
    private lateinit var cacheDir: Directory

    @BeforeEach
    fun setup() {
        cacheDir = Directory(tempFolder, "drillCache").apply {
            deleteRecursively()
            mkdir()
        }
        agentCache = AgentCacheImpl(cacheDir)
        agentRunner = AgentRunner(agentInstaller, agentCache)
    }

    @Test
    fun `given zipPath, getJvmOptionsToRun should unzip agent by zipPath and return agentpath and other expected options`() {
        val distDir = Directory(tempFolder, "distDir")
        val agentZipFile = File(distDir, "agent.zip")

        val configuration = TestAgentConfiguration().apply {
            zipPath = agentZipFile
            additionalParams = mapOf("arg1" to "value1")
        }
        val agentDir = Directory(distDir, "agent")
        val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
        whenever(agentInstaller.unzip(agentZipFile, distDir)).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(distDir, configuration)
        }

        assertEquals(
            "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
            result.first { it.startsWith("-agentpath:") })
        verify(agentInstaller).unzip(agentZipFile, distDir)
        verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)
    }

    @Test
    fun `given downloadUrl that is not in cache, getJvmOptionsToRun should download agent by url and return agentpath and other expected options`() {
        val agentDownloadUrl = "http://example.com/agent.zip"
        val configuration = TestAgentConfiguration().apply {
            downloadUrl = agentDownloadUrl
            additionalParams = mapOf("arg1" to "value1")
        }
        val distDir = Directory(tempFolder, "distDir")
        val agentDir = Directory(distDir, "agent")
        val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
        wheneverBlocking(agentInstaller) { download(any(), any()) }.thenReturn(anyFile())
        whenever(agentInstaller.unzip(any(), eq(distDir))).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(distDir, configuration)
        }

        assertEquals(
            "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
            result.first { it.startsWith("-agentpath:") })
        verifyBlocking(agentInstaller) { download(any(), any()) }
        verify(agentInstaller).unzip(any(), eq(distDir))
        verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)
    }

    @Test
    fun `given downloadUrl that is in cache, getJvmOptionsToRun should get agent from cache and return agentpath and other expected options`() {
        val agentDownloadUrl = "http://example.com/agent.zip"
        val configuration = TestAgentConfiguration().apply {
            downloadUrl = agentDownloadUrl
            additionalParams = mapOf("arg1" to "value1")
        }
        val cachedFile = File(cacheDir, "${configuration.agentName}-${currentOsPreset}-${agentDownloadUrl.hashCode()}.zip")
        cachedFile.createNewFile()
        cachedFile.writeBytes(anyContent())
        val distDir = Directory(tempFolder, "distDir")
        val agentDir = Directory(distDir, "agent")
        val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
        whenever(agentInstaller.unzip(eq(cachedFile), eq(distDir))).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(distDir, configuration)
        }

        assertEquals(
            "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
            result.first { it.startsWith("-agentpath:") })
        verifyBlocking(agentInstaller, never()) { download(any(), any()) }
        verify(agentInstaller).unzip(any(), eq(distDir))
        verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)
    }

    @Test
    fun `given version that is not in cache, getJvmOptionsToRun should download agent by version and return agentpath and other expected options`() {
        val agentVersion = "1.0.0"
        val configuration = TestAgentConfiguration().apply {
            version = agentVersion
            additionalParams = mapOf("arg1" to "value1")
        }
        val distDir = Directory(tempFolder, "distDir")
        val agentDir = Directory(distDir, "agent")
        val agentLibFile = File(agentDir, "agent.so")
        val downloadUrl = "http://example.com/agent.zip"
        wheneverBlocking(agentInstaller) { getDownloadUrl(any(), any(), any()) }.thenReturn(
            FileUrl(filename = "agent.zip", url = downloadUrl)
        )
        wheneverBlocking(agentInstaller) { download(any(), any()) }.thenReturn(anyFile())
        whenever(agentInstaller.unzip(any(), eq(distDir))).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(any(), eq(currentOsLibExt))).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(distDir, configuration)
        }

        assertEquals(
            "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
            result.first()
        )
        verifyBlocking(agentInstaller) { getDownloadUrl(configuration.githubRepository, agentVersion, currentOsPreset) }
        verifyBlocking(agentInstaller) { download(any(), any()) }
        verify(agentInstaller).unzip(any(), eq(distDir))
        verify(agentInstaller).findAgentFile(any(), eq(currentOsLibExt))
    }

    @Test
    fun `given version that is in cache, getJvmOptionsToRun should get agent from cache and return agentpath and other expected options`() {
        val agentVersion = "1.0.0"
        val configuration = TestAgentConfiguration().apply {
            version = agentVersion
            additionalParams = mapOf("arg1" to "value1")
        }
        val cachedFile = File(cacheDir, "${configuration.agentName}-${currentOsPreset}-${agentVersion}.zip")
        cachedFile.createNewFile()
        cachedFile.writeBytes(anyContent())
        val distDir = Directory(tempFolder, "distDir")
        val agentDir = Directory(distDir, "agent")
        val agentLibFile = File(agentDir, "agent.so")
        whenever(agentInstaller.unzip(eq(cachedFile), eq(distDir))).thenReturn(agentDir)
        whenever(agentInstaller.findAgentFile(any(), eq(currentOsLibExt))).thenReturn(agentLibFile)

        val result = runBlocking {
            agentRunner.getJvmOptionsToRun(distDir, configuration)
        }

        assertEquals(
            "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
            result.first()
        )
        verifyBlocking(agentInstaller, never()) { getDownloadUrl(any(), any(), any()) }
        verifyBlocking(agentInstaller, never()) { download(any(), any()) }
        verify(agentInstaller).unzip(any(), eq(distDir))
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

private fun <M, T> wheneverBlocking(mock: M, methodCall: suspend M.() -> T): OngoingStubbing<T> {
    return runBlocking { whenever(mock.methodCall()) }
}

private fun anyFile() = File("agent.zip")

private fun anyContent() = byteArrayOf(1, 2, 3)
