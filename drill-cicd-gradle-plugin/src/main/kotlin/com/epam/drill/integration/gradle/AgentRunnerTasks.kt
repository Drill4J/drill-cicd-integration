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
import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import com.epam.drill.integration.common.agent.config.AppAgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.getJavaAddOpensOptions
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.testing.Test
import org.gradle.process.JavaForkOptions
import java.io.File

fun modifyToRunDrillAgents(
    task: Task,
    project: Project,
    pluginConfig: DrillPluginExtension
) {
    val taskConfig = task.extensions.findByType(DrillTaskExtension::class.java)
    val gitClient = GitClientImpl()

    task.doFirst {
        logger.lifecycle("Task :${task.name} is modified by Drill")

        val agentCache = AgentCacheImpl(drillAgentFilesDir)
        val agentInstaller = AgentInstallerImpl(agentCache)
        val agentRunner = AgentRunner(agentInstaller)
        val distDir = File(project.buildDir, "/drill")


        listOfNotNull(
            taskConfig?.testAgent
                ?.takeIf { it.enabled ?: pluginConfig.testAgent.enabled ?: false }
                ?.let {
                    TestAgentConfiguration().apply {
                        mapGeneralAgentProperties(it, pluginConfig.testAgent, pluginConfig)
                        this.testTaskId = it.testTaskId ?: generateTestTaskId(project)
                    }
                },
            taskConfig?.appAgent
                ?.takeIf { it.enabled ?: pluginConfig.appAgent.enabled ?: false }
                ?.let {
                    AppAgentConfiguration().apply {
                        mapGeneralAgentProperties(it, pluginConfig.appAgent, pluginConfig)
                        this.appId = pluginConfig.appId
                        this.packagePrefixes = pluginConfig.packagePrefixes
                        this.buildVersion = pluginConfig.buildVersion
                        this.commitSha = runCatching {
                            gitClient.getCurrentCommitSha()
                        }.onFailure {
                            logger.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
                        }.getOrNull()
                        if (task is Test) {
                            task.testClassesDirs.joinToString(separator = ";") { "!" + it.absolutePath }
                                .let { excludePaths ->
                                    this.additionalParams = mapOf(
                                        "scanClassPath" to excludePaths
                                    ) + (additionalParams ?: emptyMap())
                                }
                        }
                    }
                }
        ).map { config ->
            runBlocking {
                agentRunner.getJvmOptionsToRun(
                    File(distDir, config.agentName),
                    config
                )
            }
        }.flatten().let {
            getJavaAddOpensOptions() + it
        }.run {
            (task as JavaForkOptions).jvmArgs.let { previousJvmArgs ->
                if (previousJvmArgs != null) {
                    (task as JavaForkOptions).jvmArgs = this + previousJvmArgs
                } else {
                    (task as JavaForkOptions).jvmArgs = this.toMutableList()
                }
            }
            logger.info("JVM args $this have been added to :${task.name} task")
        }
    }
}

private fun Task.generateTestTaskId(project: Project) = "${project.group}:${project.name}:${this.name}"

private fun AgentConfiguration.mapGeneralAgentProperties(
    agentTaskExtension: AgentExtension,
    agentPluginExtension: AgentExtension,
    pluginExtension: DrillPluginExtension
) {
    (agentTaskExtension.takeIf {
        it.version != null || it.zipPath != null || it.downloadUrl != null
    } ?: agentPluginExtension).let { agentExtension ->
        this.version = agentExtension.version
        this.downloadUrl = agentExtension.downloadUrl
        this.zipPath = agentExtension.zipPath?.let { File(it) }
    }

    this.logLevel = agentTaskExtension.logLevel ?: agentPluginExtension.logLevel
    this.logFile = (agentTaskExtension.logFile ?: agentPluginExtension.logFile)?.let { File(it) }

    this.apiUrl = pluginExtension.apiUrl
    this.apiKey = pluginExtension.apiKey
    this.groupId = pluginExtension.groupId

    this.additionalParams = agentPluginExtension.additionalParams + agentTaskExtension.additionalParams
}