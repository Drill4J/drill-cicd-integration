package com.epam.drill.integration.gradle

import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.ResolutionScope


@Mojo(
    name = "clearAgentFileCache",
    defaultPhase = LifecyclePhase.NONE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class ClearAgentFileCacheMojo : AbstractDrillMojo() {

    override fun execute() {
        val agentCache = AgentCacheImpl(drillAgentFilesDir)
        agentCache.clearAll()
        log.info("Agent file cache has been cleared")
    }
}