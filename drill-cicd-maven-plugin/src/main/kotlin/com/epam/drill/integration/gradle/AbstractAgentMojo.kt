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
package com.epam.drill.integration.gradle

import com.epam.drill.integration.common.agent.AgentRunner
import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import java.io.File


private const val ARG_LINE = "argLine"

abstract class AbstractAgentMojo: AbstractDrillMojo() {
    private val agentFileDir = File(System.getProperty("user.home"), ".drill/agents")
    private val agentInstaller = AgentInstallerImpl()
    private val agentCache = AgentCacheImpl(agentFileDir)
    private val agentRunner = AgentRunner(agentInstaller, agentCache)

    abstract fun getAgentConfig(): AgentConfiguration

    override fun execute() {
        val distDir = File(project.build?.directory, "/drill").absolutePath
        val config = getAgentConfig()
        val jvmOptions = runBlocking {
            agentRunner.getJvmOptionsToRun(
                File(distDir, config.agentName),
                config
            )
        }

        val oldArgLine = project.properties.getProperty(ARG_LINE) ?: ""
        val newArgLine = "$oldArgLine ${jvmOptions.joinToString(" ")}".trim()
        project.properties.setProperty(ARG_LINE, newArgLine)
        log.info("JVM args $jvmOptions have been added to the goal")
    }
}

fun AgentConfiguration.setGeneralAgentProperties(
    mavenAgentConfig: AgentMavenConfiguration,
    mavenGeneralConfig: AbstractDrillMojo
) {
    version = mavenAgentConfig.version
    downloadUrl = mavenAgentConfig.downloadUrl
    zipPath = mavenAgentConfig.zipPath?.let { File(it) }

    logLevel = mavenAgentConfig.logLevel
    logFile = mavenAgentConfig.logFile?.let { File(it) }

    apiUrl = mavenGeneralConfig.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
    apiKey = mavenGeneralConfig.apiKey.fromEnv("DRILL_API_KEY")
    groupId = mavenGeneralConfig.groupId.required("groupId")

    additionalParams = mavenAgentConfig.additionalParams
}