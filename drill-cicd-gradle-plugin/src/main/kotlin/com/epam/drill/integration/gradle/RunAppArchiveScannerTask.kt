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
import com.epam.drill.integration.common.agent.config.AppAgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.agent.impl.JarCommandLineBuilder
import com.epam.drill.integration.common.agent.javaExecutable
import com.epam.drill.integration.common.git.impl.GitClientImpl
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.io.File
import org.gradle.api.tasks.testing.Test

fun Task.drillScanAppArchive(config: DrillPluginExtension) {
    doFirst {
        val archiveFiles = collectArchiveFiles(project)
        if (archiveFiles.isEmpty()) {
            logger.error("No archive files found in the project. " +
                    "Please ensure that your build produces an archive (e.g., JAR, WAR, etc.).")
            return@doFirst
        }
        scanAppArchive(
            archiveFilesPaths = archiveFiles.map{ it.absolutePath },
            project = project,
            pluginConfig = config
        )
    }
}

private fun collectArchiveFiles(project: Project): List<File> {
    return project.tasks
        .withType(AbstractArchiveTask::class.java)
        .mapNotNull { task ->
            task.archiveFile.orNull?.asFile?.takeIf { it.exists() }
        }
}


fun modifyToRunAppArchiveScanner(
    task: Task,
    project: Project,
    pluginConfig: DrillPluginExtension
) {
    val taskConfig = task.extensions.findByType(DrillTaskExtension::class.java)

    task.doLast {
        taskConfig?.appAgent?.takeIf {
            it.archiveScannerEnabled ?: pluginConfig.appAgent.archiveScannerEnabled ?: false
        }?.let {
            logger.lifecycle("Task :${task.name} is modified to scan application archive by Drill4J")
            if (!state.didWork) {
                logger.lifecycle("Skipping ${task.name}: up-to-date or no work required")
            }
            scanAppArchive(
                archiveFilesPaths = outputs.files.files.map { it.absolutePath },
                project = project,
                pluginConfig = pluginConfig,
                taskConfig = taskConfig,
                testTask = task
            )
        }
    }
}

fun Task.scanAppArchive(
    archiveFilesPaths: Collection<String>,
    project: Project,
    pluginConfig: DrillPluginExtension,
    taskConfig: DrillTaskExtension? = null,
    testTask: Task? = null
) {
    val gitClient = GitClientImpl()

    val agentCache = AgentCacheImpl(drillAgentFilesDir)
    val agentInstaller = AgentInstallerImpl(agentCache)
    val argumentsBuilder = JarCommandLineBuilder()
    val commandExecutor = CommandExecutor(javaExecutable.absolutePath)
    val executableRunner = ExecutableRunner(agentInstaller, argumentsBuilder, commandExecutor)
    val distDir = File(project.buildDir, "/drill")

    AppAgentConfiguration().apply {
        mapGeneralAgentProperties(
            agentTaskExtension = taskConfig?.appAgent ?: AppAgentExtension(),
            agentPluginExtension = pluginConfig.appAgent,
            pluginExtension = pluginConfig
        )
        this.appId = pluginConfig.appId

        this.packagePrefixes = pluginConfig.packagePrefixes
        this.buildVersion = pluginConfig.buildVersion
        this.commitSha = runCatching {
            gitClient.getCurrentCommitSha()
        }.onFailure {
            logger.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
        }.getOrNull()
        
        this.classScanningEnabled = true

        val scanClassPaths = mutableListOf<String>()
        scanClassPaths.addAll(archiveFilesPaths)
        if (testTask is Test) {
            scanClassPaths.addAll(testTask.classpath.map{ it.absolutePath })
            scanClassPaths.addAll(testTask.testClassesDirs.map{ "!" + it.absolutePath })
        }
        this.scanClassPath = scanClassPaths.joinToString (";")

    }.let { config ->
        runBlocking {
            if (archiveFilesPaths.isNotEmpty()) {
                logger.lifecycle("App archive scanner running for files ${archiveFilesPaths.joinToString(", ") { it }} ...")
            }
            executableRunner.runScan(config, distDir) { line ->
                logger.lifecycle(line)
            }.also { exitCode ->
                logger.lifecycle("App archive scanner exited with code $exitCode")
            }
        }
    }
}
