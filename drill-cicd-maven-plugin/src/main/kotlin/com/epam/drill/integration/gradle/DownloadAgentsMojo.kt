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

import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.agent.config.AppAgentConfiguration
import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(
    name = "downloadAgents",
    defaultPhase = LifecyclePhase.NONE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class DownloadAgentsMojo : AbstractDrillMojo() {
    private val agentCache = AgentCacheImpl(drillAgentFilesDir)
    private val agentInstaller = AgentInstallerImpl(agentCache)

    @Parameter(property = "testAgent", required = false)
    var testAgent: TestAgentMavenConfiguration? = null
    @Parameter(property = "appAgent", required = false)
    var appAgent: AppAgentMavenConfiguration? = null

    override fun execute() {
        appAgent?.let {
            val agentConfig = AppAgentConfiguration()
            val agentName = agentConfig.agentName
            val githubRepository = agentConfig.githubRepository
            download(it, agentName, githubRepository)
        }
        testAgent?.let {
            val agentConfig = TestAgentConfiguration()
            val agentName = agentConfig.agentName
            val githubRepository = agentConfig.githubRepository
            download(it, agentName, githubRepository)
        }
    }

    private fun download(
        agentConfig: AgentMavenConfiguration,
        agentName: String,
        githubRepository: String
    ) {
        val downloadUrl = agentConfig.downloadUrl
        val version = agentConfig.version

        when {
            downloadUrl != null -> runBlocking {
                agentInstaller.downloadByUrl(
                    downloadUrl, agentName
                ).also {
                    log.info("Agent ${it.name} has been downloaded")
                }
            }

            version != null -> runBlocking {
                agentInstaller.downloadByVersion(
                    githubRepository, version, agentName
                )
            }.also {
                log.info("Agent ${it.name} has been downloaded")
            }
        }
    }
}