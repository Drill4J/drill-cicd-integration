package com.epam.drill.integration.common.agent

import java.io.File

interface AgentCache {
    fun clearAll()
    fun clear(agentName: String, version: String, preset: String)
    suspend fun get(
        agentName: String,
        version: String,
        preset: String,
        download: suspend (filename: String, downloadDir: Directory) -> Unit
    ): File
}