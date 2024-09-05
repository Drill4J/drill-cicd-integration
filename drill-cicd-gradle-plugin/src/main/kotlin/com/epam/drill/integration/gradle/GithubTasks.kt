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

import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.github.client.impl.GithubApiClientImpl
import com.epam.drill.integration.github.service.GithubCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task
import java.io.File


fun Task.drillGithubPullRequestReport(config: DrillPluginExtension) {
    doFirst {
        val github = config.github.required("github")
        val githubApiUrl = github.apiUrl.required("github.apiUrl")
        val githubToken = github.token.required("github.token")
        val apiUrl = config.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
        val apiKey = config.apiKey.fromEnv("DRILL_API_KEY")
        val groupId = config.groupId.required("groupId")
        val appId = config.appId.required("appId")
        val eventFilePath = github.eventFilePath
            .fromEnv("GITHUB_EVENT_PATH")
            .required("github.eventFilePath")

        val githubCiCdService = GithubCiCdService(
            GithubApiClientImpl(
                githubApiUrl,
                githubToken,
            ),
            MetricsClientImpl(
                apiUrl,
                apiKey
            ),
            MarkdownReportGenerator(),
            GitClientImpl()
        )
        logger.lifecycle("Posting Drill4J Testing Report for $groupId/$appId to GitHub by event file $eventFilePath...")
        runBlocking {
            githubCiCdService.postPullRequestReportByEvent(
                groupId = groupId,
                appId = appId,
                githubEventFile = File(eventFilePath)
            )
        }
    }
}