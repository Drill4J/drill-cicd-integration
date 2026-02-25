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

import com.epam.drill.integration.common.agent.CommandExecutor
import com.epam.drill.integration.common.agent.ExecutableRunner
import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.agent.impl.JarCommandLineBuilder
import com.epam.drill.integration.common.agent.javaExecutable
import com.epam.drill.integration.common.git.impl.GitClientImpl
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.io.File

internal fun collectArchiveFiles(project: Project): FileCollection {
    return project.files(
        project.tasks
            .withType(AbstractArchiveTask::class.java)
            .mapNotNull { task ->
                task.archiveFile.orNull?.asFile?.takeIf { it.exists() }
            })
}

fun Task.modifyToScanAppArchive(
    task: Task,
    project: Project,
    pluginConfig: DrillPluginExtension,
) {
    logger.lifecycle("Task :${task.name} is modified to scan application classes by Drill4J")
    if (!state.didWork) {
        logger.lifecycle("Skipping ${task.name}: up-to-date or no work required")
        return
    }

    scanAppArchive(
        project = project,
        pluginConfig = pluginConfig
    )
}

fun Task.scanAppArchive(
    project: Project,
    pluginConfig: DrillPluginExtension,
    scanPaths: FileCollection? = null,
) {
    val task = this
    val gitClient = GitClientImpl()

    val agentCache = AgentCacheImpl(drillAgentFilesDir)
    val agentInstaller = AgentInstallerImpl(agentCache)
    val argumentsBuilder = JarCommandLineBuilder()
    val commandExecutor = CommandExecutor(javaExecutable.absolutePath)
    val executableRunner = ExecutableRunner(agentInstaller, argumentsBuilder, commandExecutor)
    val distDir = File(project.buildDir, "/drill")

    AgentConfiguration().apply {
        mapGeneralAgentProperties(pluginConfig)
        mapBuildSpecificProperties(pluginConfig, task, gitClient)
        mapClassScanningProperties(pluginConfig, task, project, scanPaths, true)
        this.messageSendingMode = "DIRECT"
        if (this.scanClassPath?.isEmpty() ?: true) {
            throw IllegalStateException("No classes or archives to scan for Drill4J Agent.")
        }
    }.let { config ->
        runBlocking {
            logger.lifecycle("Drill4J file scanner is running...")
            logger.lifecycle("Scanning: ${config.scanClassPath}")
            executableRunner.runScan(config, distDir) { line ->
                logger.lifecycle(line)
            }.also { exitCode ->
                logger.lifecycle("Drill4J file scanner exited with code $exitCode")
            }
        }
    }
}
