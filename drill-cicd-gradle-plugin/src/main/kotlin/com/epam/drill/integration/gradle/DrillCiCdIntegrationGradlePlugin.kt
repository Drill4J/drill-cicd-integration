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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.testing.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

private const val TASK_GROUP = "drill"

class DrillCiCdIntegrationGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val pluginConfig = project.extensions.create("drill", DrillPluginExtension::class.java)

        project.task("drillGitlabMergeRequestReport") {
            drillGitlabMergeRequestReportTask(pluginConfig)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillGithubPullRequestReport") {
            drillGithubPullRequestReport(pluginConfig)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillSendBuildInfo") {
            drillSendBuildInfo(pluginConfig)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillScanAppArchive") {
            doFirst {
                scanAppArchive(
                    project = project,
                    pluginConfig = pluginConfig,
                )
            }
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillGenerateChangeTestingReport") {
            drillGenerateChangeTestingReport(pluginConfig)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillClearAgentFileCache") {
            drillClearAgentFileCache(pluginConfig)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillDownloadAgents") {
            drillDownloadAgents(pluginConfig)
        }.also {
            it.group = TASK_GROUP
        }

        project.tasks.withType(Test::class.java).configureEachWithDrill { taskConfig ->
            doFirst {
                val config = merge(pluginConfig, taskConfig)
                modifyToRunDrillAgents(project, config)
                if (config.classScanning.beforeRun == true)
                    modifyToScanAppArchive(project, config)
            }
        }

        project.tasks.withType(JavaExec::class.java).configureEachWithDrill { taskConfig ->
            doFirst {
                val config = merge(pluginConfig, taskConfig)
                modifyToRunDrillAgents(project, config)
                if (config.classScanning.beforeRun == true)
                    modifyToScanAppArchive(project, config)
            }
        }

        project.tasks.withType(AbstractArchiveTask::class.java).configureEachWithDrill { taskConfig ->
            doLast {
                val config = merge(pluginConfig, taskConfig)
                if (config.classScanning.afterBuild == true)
                    modifyToScanAppArchive(project, config)
            }
        }
    }
}

private fun <T : Task> TaskCollection<T>.configureEachWithDrill(
    action: T.(taskConfig: DrillPluginExtension) -> Unit
) = configureEach {
    val taskConfig = this.extensions.create(
        "drill",
        DrillPluginExtension::class.java
    )
    action(taskConfig)
}


inline fun <reified T : Any> merge(base: T, override: T): T =
    mergeObjects(base, override, T::class) as T

@Suppress("UNCHECKED_CAST")
fun <T : Any> mergeObjects(base: T, override: T, clazz: KClass<T>): T {
    val primaryConstructor = clazz.primaryConstructor
        ?: error("Class ${clazz.simpleName} must have a primary constructor")

    val args = primaryConstructor.parameters.associateWith { param ->
        val property = clazz.memberProperties.first { it.name == param.name }
        val overrideValue = property.get(override)
        val baseValue = property.get(base)

        when {
            // Both values are classes
            overrideValue != null && baseValue != null && overrideValue::class.isSubclassOf(PluginExtension::class) -> {
                mergeObjects(baseValue, overrideValue, overrideValue::class as KClass<Any>)
            }
            // Override is not null
            overrideValue != null -> overrideValue
            // Fall back to base
            else -> baseValue
        }
    }

    return primaryConstructor.callBy(args)
}
