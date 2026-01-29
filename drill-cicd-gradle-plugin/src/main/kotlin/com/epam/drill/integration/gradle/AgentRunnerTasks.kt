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
import com.epam.drill.integration.common.agent.config.AgentMode
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.agent.impl.JavaAgentCommandLineBuilder
import com.epam.drill.integration.common.agent.impl.NativeAgentCommandLineBuilder
import com.epam.drill.integration.common.baseline.BaselineFactory
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.getJavaAddOpensOptions
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.testing.Test
import org.gradle.process.JavaForkOptions
import java.io.File
import kotlin.collections.plus

fun modifyToRunDrillAgents(
    task: Task,
    project: Project,
    pluginConfig: DrillPluginExtension
) {
    val gitClient = GitClientImpl()
    val baselineFactory = BaselineFactory(gitClient)

    task.doFirst {
        logger.lifecycle("Task :${task.name} is modified by Drill")

        val agentCache = AgentCacheImpl(drillAgentFilesDir)
        val agentInstaller = AgentInstallerImpl(agentCache)
        val distDir = File(project.buildDir, "/drill")

        AgentConfiguration().apply {
            mapGeneralAgentProperties(pluginConfig)
            mapBuildSpecificProperties(pluginConfig, task, gitClient)
            mapClassScanningProperties(pluginConfig, task, project)
            mapTestSpecificProperties(pluginConfig, task, project, gitClient, baselineFactory)
        }.let { config ->
            runBlocking {
                agentInstaller.installAgent(File(distDir, config.agentName), config)
            }.let { agentDir ->
                when (config.agentMode) {
                    AgentMode.NATIVE -> NativeAgentCommandLineBuilder()
                    AgentMode.JAVA -> JavaAgentCommandLineBuilder()
                }.build(agentDir, config)
            }
        }.let {
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

internal fun Task.generateTestTaskId(project: Project) = "${project.group}:${project.name}:${this.name}"

internal fun AgentConfiguration.mapGeneralAgentProperties(
    pluginExtension: DrillPluginExtension
) {
    pluginExtension.agent.takeIf {
        it.version != null || it.zipPath != null || it.downloadUrl != null
    }?.let { agentExtension ->
        this.version = agentExtension.version
        this.downloadUrl = agentExtension.downloadUrl
        this.zipPath = agentExtension.zipPath?.let { File(it) }
    }

    this.logLevel = pluginExtension.agent.logLevel
    this.logFile = (pluginExtension.agent.logFile)?.let { File(it) }
    this.agentMode = (pluginExtension.agent.agentMode)?.let { AgentMode.valueOf(it) } ?: AgentMode.NATIVE

    this.apiUrl = pluginExtension.apiUrl
    this.apiKey = pluginExtension.apiKey
    this.groupId = pluginExtension.groupId
    this.appId = pluginExtension.appId
    this.envId = pluginExtension.envId
    this.packagePrefixes = pluginExtension.packagePrefixes
    this.additionalParams = pluginExtension.additionalParams
}

internal fun AgentConfiguration.mapBuildSpecificProperties(
    pluginExtension: DrillPluginExtension,
    task: Task,
    gitClient: GitClient
) {
    this.buildVersion = pluginExtension.buildVersion
    this.commitSha = runCatching {
        gitClient.getCurrentCommitSha()
    }.onFailure {
        task.logger.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
    }.getOrNull()
}

internal fun AgentConfiguration.mapClassScanningProperties(
    pluginExtension: DrillPluginExtension,
    task: Task,
    project: Project,
    classPaths: FileCollection? = null
) {
    this.classScanningEnabled = pluginExtension.classScanning.enabled && pluginExtension.classScanning.runtime
    val appClasses: FileCollection? = pluginExtension.classScanning.appClasses ?: classPaths ?: when (task) {
        is Test -> task.classpath
        is JavaExec -> task.classpath
        is AbstractArchiveTask -> {
            task.archiveFile.orNull?.asFile?.takeIf { it.exists() }?.let { project.files(it) }
        }
        else -> null
    }
    if (appClasses == null || appClasses.isEmpty) when (task) {
        is Test, is JavaExec -> task.logger.error("No files found on task's classpath")
        is AbstractArchiveTask -> task.logger.error("No archive files found. Please ensure that your build produces an archive (JAR, WAR)")
    }
    val testClasses: FileCollection? = pluginExtension.classScanning.testClasses ?: when (task) {
        is Test -> task.testClassesDirs
        else -> null
    }
    val appClassPaths = appClasses?.filterNot { testClasses?.contains(it) ?: false }?.map { it.absolutePath } ?: emptyList()
    val testClassPaths = testClasses?.map { "!" + it.absolutePath } ?: emptyList()
    this.scanClassPath = (appClassPaths + testClassPaths).joinToString(";")
}

internal fun AgentConfiguration.mapTestSpecificProperties(
    pluginExtension: DrillPluginExtension,
    task: Task,
    project: Project,
    gitClient: GitClient,
    baselineFactory: BaselineFactory
) {
    this.testTaskId = pluginExtension.testTaskId ?: task.generateTestTaskId(project)
    this.testTracingEnabled = pluginExtension.testTracking.enabled && pluginExtension.coverage.perTestLaunch
    this.testLaunchMetadataSendingEnabled = pluginExtension.testTracking.enabled
    this.recommendedTestsEnabled = pluginExtension.recommendedTests.enabled
    if (this.recommendedTestsEnabled == true) {
        this.recommendedTestsCoveragePeriodDays = pluginExtension.recommendedTests.coveragePeriodDays
        this.recommendedTestsTargetAppId = pluginExtension.appId
        this.recommendedTestsTargetCommitSha = runCatching {
            gitClient.getCurrentCommitSha()
        }.onFailure {
            task.logger.warn("Unable to retrieve the current commit SHA. The 'recommendedTestsTargetCommitSha' parameter will not be set. Error: ${it.message}")
        }.getOrNull()
        this.recommendedTestsTargetBuildVersion = pluginExtension.buildVersion
        pluginExtension.baseline.searchStrategy?.let { searchStrategy ->
            val baselineTagPattern = pluginExtension.baseline.tagPattern ?: "*"
            val baselineTargetRef = pluginExtension.baseline.targetRef
            val searchCriteria = when (searchStrategy) {
                BaselineSearchStrategy.SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
                BaselineSearchStrategy.SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baselineTargetRef"))
            }
            this.recommendedTestsBaselineCommitSha =
                baselineFactory.produce(searchStrategy).findBaseline(searchCriteria)
        }
    }
}