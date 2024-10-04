package com.epam.drill.integration.gradle

import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import org.gradle.api.Task
import java.io.File

val drillAgentFilesDir = File(System.getProperty("user.home"), ".drill/agents")

fun Task.drillClearAgentFileCache(config: DrillPluginExtension) {
    doFirst {
        val agentCache = AgentCacheImpl(drillAgentFilesDir)
        agentCache.clearAll()
        logger.lifecycle("Agent file cache has been cleared")
    }
}