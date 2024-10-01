package com.epam.drill.integration.common.agent

import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AgentRunnerTest {

    private val agentInstaller = mock(AgentInstaller::class.java)
    private val agentRunner = AgentRunner(agentInstaller)

    @field:TempDir
    lateinit var tempFolder: File

    @Test
    fun `given zipPath, getJvmOptionsToRun should unzip agent by zipPath and return agentpath and other expected options`() {
        val distDir = Directory(tempFolder, "distDir")
        val agentZipFile = File(distDir, "agent.zip")
        runBlocking {
            val configuration = TestAgentConfiguration().apply {
                zipPath = agentZipFile
                additionalParams = mapOf("arg1" to "value1")
            }
            val agentDir = Directory(distDir, "agent")
            val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
            whenever(agentInstaller.unzip(agentZipFile, distDir)).thenReturn(agentDir)
            whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)

            val result = agentRunner.getJvmOptionsToRun(distDir, configuration)

            assertEquals(
                "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
                result.first { it.startsWith("-agentpath:") })
            verify(agentInstaller).unzip(agentZipFile, distDir)
            verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)

        }
    }

    @Test
    fun `given downloadUrl, getJvmOptionsToRun should download agent by url and return agentpath and other expected options`() {
        val distDir = Directory(tempFolder, "distDir")
        val agentDownloadUrl = "http://example.com/agent.zip"
        runBlocking {

            val configuration = TestAgentConfiguration().apply {
                downloadUrl = agentDownloadUrl
                additionalParams = mapOf("arg1" to "value1")
            }
            val agentUrl = FileUrl(filename = "$currentOsPreset.zip", url = agentDownloadUrl)
            val agentZipFile = File(distDir, "agent.zip")
            val agentDir = Directory(distDir, "agent")
            val agentLibFile = File(agentDir, "agent.$currentOsLibExt")
            whenever(agentInstaller.download(agentUrl, distDir)).thenReturn(agentZipFile)
            whenever(agentInstaller.unzip(agentZipFile)).thenReturn(agentDir)
            whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)


            val result = agentRunner.getJvmOptionsToRun(distDir, configuration)

            assertEquals(
                "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
                result.first { it.startsWith("-agentpath:") })
            verify(agentInstaller).download(agentUrl, distDir)
            verify(agentInstaller).unzip(agentZipFile)
            verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)

        }
    }

    @Test
    fun `given version, getJvmOptionsToRun should download agent by version and return agentpath and other expected options`() {
        val agentVersion = "1.0.0"
        val distDir = Directory(tempFolder, "distDir")
        runBlocking {
            val configuration = TestAgentConfiguration().apply {
                version = agentVersion
                additionalParams = mapOf("arg1" to "value1")
            }
            val agentUrl = FileUrl(filename = "agent.zip", url = "http://example.com/agent.zip")
            val agentZipFile = File(distDir, "agent.zip")
            val agentDir = Directory(distDir, "agent")
            val agentLibFile = File(agentDir, "agent.so")
            whenever(
                agentInstaller.getDownloadUrl(
                    configuration.githubRepository,
                    agentVersion,
                    currentOsPreset
                )
            ).thenReturn(agentUrl)
            whenever(agentInstaller.download(agentUrl, distDir)).thenReturn(agentZipFile)
            whenever(agentInstaller.unzip(agentZipFile)).thenReturn(agentDir)
            whenever(agentInstaller.findAgentFile(agentDir, currentOsLibExt)).thenReturn(agentLibFile)


            val result = agentRunner.getJvmOptionsToRun(distDir, configuration)

            assertEquals(
                "-agentpath:$agentLibFile=drillInstallationDir=$agentDir,arg1=value1",
                result.first { it.startsWith("-agentpath:") })
            verify(agentInstaller).getDownloadUrl(configuration.githubRepository, agentVersion, currentOsPreset)
            verify(agentInstaller).download(agentUrl, distDir)
            verify(agentInstaller).unzip(agentZipFile)
            verify(agentInstaller).findAgentFile(agentDir, currentOsLibExt)
        }
    }

    @Test
    fun `given configuration without required fields, getJvmOptionsToRun should throw IllegalStateException`() {
        val distDir = mock(Directory::class.java)
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