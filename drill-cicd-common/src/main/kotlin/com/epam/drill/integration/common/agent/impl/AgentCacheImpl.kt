package com.epam.drill.integration.common.agent.impl

import com.epam.drill.integration.common.agent.AgentCache
import com.epam.drill.integration.common.agent.Directory
import com.epam.drill.integration.common.agent.FileUrl
import java.io.File

class AgentCacheImpl(
    private val cacheDir: Directory
) : AgentCache {


    override fun clearAll() {
        cacheDir.deleteRecursively()
    }

    override fun clear(agentName: String, version: String, preset: String) {
        val file = File(cacheDir, getAgentFilename(agentName, preset, version))
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun get(
        agentName: String,
        version: String,
        preset: String,
        download: suspend (filename: String, downloadDir: Directory) -> Unit
    ): File {
        val file = File(cacheDir, getAgentFilename(agentName, preset, version))
        if (!file.exists()) {
            download(file.name, cacheDir)
        }
        return file
    }

    private fun getAgentFilename(agentName: String, preset: String, version: String) =
        "$agentName-$preset-$version.zip"
}