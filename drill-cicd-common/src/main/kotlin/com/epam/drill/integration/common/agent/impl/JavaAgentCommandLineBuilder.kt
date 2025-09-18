package com.epam.drill.integration.common.agent.impl

import com.epam.drill.integration.common.agent.CommandLineBuilder
import com.epam.drill.integration.common.agent.Directory
import com.epam.drill.integration.common.agent.config.AgentConfiguration
import java.io.File

class JavaAgentCommandLineBuilder : CommandLineBuilder {
    override fun build(
        agentDir: Directory,
        configuration: AgentConfiguration
    ): List<String> {
        val javaAgentFile = findJarFile(agentDir)
        return listOf(
            "-javaagent:${javaAgentFile.absolutePath}="
                    + "drillInstallationDir=${javaAgentFile.parent ?: ""},"
                    + getArgsMap(configuration).map { (k, v) -> "$k=$v" }.joinToString(",")
        )
    }

    private fun findJarFile(agentDir: Directory): File {
        return findFile(agentDir, "jar")
            ?: throw IllegalStateException("No jar file found in the agent directory: ${agentDir.path}")
    }

    private fun getArgsMap(configuration: AgentConfiguration) = configuration.toAgentArguments()
        .filterValues { !it.isNullOrEmpty() }

}