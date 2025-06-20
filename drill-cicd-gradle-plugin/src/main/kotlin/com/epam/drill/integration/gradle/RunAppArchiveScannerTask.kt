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

import com.epam.drill.integration.common.agent.ExecutableRunner
import com.epam.drill.integration.common.agent.config.AppArchiveScannerConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.testing.Test
import java.io.File

fun modifyToRunAppArchiveScanner(
    task: Task,
    project: Project,
    pluginConfig: DrillPluginExtension
) {
    val taskConfig = task.extensions.findByType(DrillTaskExtension::class.java)
    val gitClient = GitClientImpl()

    task.doLast {
        logger.lifecycle("Task :${task.name} is modified to scan application archive by Drill4J")

        val archive = task.outputs.files.singleFile
        if (state.didWork) {
            println("Analyzing ${task.name}: ${archive.absolutePath}")
        } else {
            println("Skipping ${task.name}: up-to-date or no work required")
        }

        val agentCache = AgentCacheImpl(drillAgentFilesDir)
        val agentInstaller = AgentInstallerImpl(agentCache)
        val executableRunner = ExecutableRunner(agentInstaller)
        val distDir = File(project.buildDir, "/drill")
        taskConfig?.appArchiveScanner
            ?.takeIf {
                it.enabled ?: pluginConfig.appArchiveScanner.enabled ?: false
            }
            ?.let {
                AppArchiveScannerConfiguration().apply {
                    mapGeneralAgentProperties(it, pluginConfig.appArchiveScanner, pluginConfig)
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
            ?.let { config ->
                runBlocking {
                    val exitCode = executableRunner.runScan(config, distDir, archive) { line ->
                        println(line)
                    }
                    println("App archive scanner exited with code $exitCode")
                }
            }
    }

}
