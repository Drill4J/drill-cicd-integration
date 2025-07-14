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
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.process.JavaForkOptions
import kotlin.reflect.KClass

private const val TASK_GROUP = "drill"
private val taskType: Set<KClass<out JavaForkOptions>> = setOf(Test::class, JavaExec::class)

class DrillCiCdIntegrationGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.extensions.create("drill", DrillPluginExtension::class.java)

        project.task("drillGitlabMergeRequestReport") {
            drillGitlabMergeRequestReportTask(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillGithubPullRequestReport") {
            drillGithubPullRequestReport(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillSendBuildInfo") {
            drillSendBuildInfo(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillGenerateChangeTestingReport") {
            drillGenerateChangeTestingReport(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillGenerateImpactedTestsReport") {
            drillGenerateImpactedTestsReport(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillClearAgentFileCache") {
            drillClearAgentFileCache(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.task("drillDownloadAgents") {
            drillDownloadAgents(config)
        }.also {
            it.group = TASK_GROUP
        }

        project.tasks.withType(Test::class.java) {
            extensions.create("drill", DrillTaskExtension::class.java)
        }

        project.tasks.withType(JavaExec::class.java) {
            extensions.create("drill", DrillTaskExtension::class.java)
        }

        project.afterEvaluate {
            tasks
                .filter { taskType.any { taskType -> taskType.java.isInstance(it) } }
                .filter { it is JavaForkOptions }
                .forEach { task ->
                    modifyToRunDrillAgents(task, project, config)
                }
        }
    }
}


