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

import com.epam.drill.integration.common.agent.config.AppAgentConfiguration
import com.epam.drill.integration.common.agent.config.AppArchiveScannerConfiguration
import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task
import java.io.File

val drillAgentFilesDir = File(System.getProperty("user.home"), ".drill/agents")

fun Task.drillClearAgentFileCache(config: DrillPluginExtension) {
    val agentCache = AgentCacheImpl(drillAgentFilesDir)

    doFirst {
        agentCache.clearAll()
        logger.lifecycle("Agent file cache has been cleared")
    }
}

fun Task.drillDownloadAgents(config: DrillPluginExtension) {
    val agentCache = AgentCacheImpl(drillAgentFilesDir)
    val agentInstaller = AgentInstallerImpl(agentCache)

    fun download(
        agentConfig: AgentExtension,
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
                    logger.lifecycle("Agent ${it.name} has been downloaded")
                }
            }

            version != null -> runBlocking {
                agentInstaller.downloadByVersion(
                    githubRepository, agentName, version
                )
            }.also {
                logger.lifecycle("Agent ${it.name} has been downloaded")
            }
        }
    }

    doFirst {
        config.appAgent.enabled?.takeIf { it }?.let {
            val agentConfig = AppAgentConfiguration()
            val agentName = agentConfig.agentName
            val githubRepository = agentConfig.githubRepository
            download(config.appAgent, agentName, githubRepository)
        }
        config.testAgent.enabled?.takeIf { it }?.let {
            val agentConfig = TestAgentConfiguration()
            val agentName = agentConfig.agentName
            val githubRepository = agentConfig.githubRepository
            download(config.testAgent, agentName, githubRepository)
        }
        config.appArchiveScanner.enabled?.takeIf { it }?.let {
            val agentConfig = AppArchiveScannerConfiguration()
            val agentName = agentConfig.agentName
            val githubRepository = agentConfig.githubRepository
            download(config.testAgent, agentName, githubRepository)
        }
    }
}