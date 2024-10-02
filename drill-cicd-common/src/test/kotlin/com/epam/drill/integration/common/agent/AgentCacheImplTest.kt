package com.epam.drill.integration.common.agent

import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class AgentCacheImplTest {

    @field:TempDir
    lateinit var cacheDir: Directory
    lateinit var agentCache: AgentCacheImpl

    @BeforeEach
    fun setup() {
        agentCache = AgentCacheImpl(cacheDir)
    }

    @Test
    fun clearAll_deletesCacheDir() {
        val file1 = createSomeFile("agent1-preset-1.0.0.zip")
        val file2 = createSomeFile("agent2-preset-2.0.0.zip")

        agentCache.clearAll()

        assertFalse(file1.exists())
        assertFalse(file2.exists())
    }

    @Test
    fun clear_deletesSpecificAgentFile() {
        val agentName = "agent"
        val version = "1.0.0"
        val preset = "preset"
        val file = createSomeFile("agent-preset-1.0.0.zip")

        agentCache.clear(agentName, version, preset)

        assertFalse(file.exists())
    }

    @Test
    fun get_downloadsFileIfNotExists() {
        val agentName = "agent"
        val version = "1.0.0"
        val preset = "preset"
        val fileContent = anyContent()

        val result = runBlocking {
            agentCache.get(agentName, version, preset) { filename, downloadDir ->
                val file = downloadDir.resolve(filename)
                file.createNewFile()
                file.writeBytes(fileContent)
            }
        }

        assertEquals(fileContent, result.readBytes())
    }

    @Test
    fun get_returnsFileIfExists() {
        val agentName = "agent"
        val version = "1.0.0"
        val preset = "preset"
        val file = createSomeFile("agent-preset-1.0.0.zip")

        val result = runBlocking {
            agentCache.get(agentName, version, preset) { _, _ ->
                fail("Should not be called")
            }
        }

        assertEquals(file.readBytes(), result.readBytes())
    }

    private fun createSomeFile(filename: String): File {
        val file = cacheDir.resolve(filename)
        file.createNewFile()
        file.writeBytes(anyContent())
        return file
    }

    private fun anyContent() = byteArrayOf(1, 2, 3)

    private fun assertEquals(expected: ByteArray, actual: ByteArray) {
        assertEquals(expected.toList(), actual.toList())
    }
}
