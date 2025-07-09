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
package com.epam.drill.integration.github.service

import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.report.ReportFormat
import com.epam.drill.integration.common.report.ReportGenerator
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.github.client.GithubApiClient
import com.epam.drill.integration.github.model.GithubEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import mu.KotlinLogging
import java.io.File

class GithubCiCdService(
    private val githubApiClient: GithubApiClient,
    private val metricsClient: MetricsClient,
    private val reportGenerator: ReportGenerator,
    private val gitClient: GitClient
) {
    private val logger = KotlinLogging.logger { }

    suspend fun postPullRequestReport(
        githubRepository: String,
        githubPullRequestId: Int,
        groupId: String,
        appId: String,
        headCommitSha: String,
        mergeBaseCommitSha: String,
        useMaterializedViews: Boolean? = null
    ) {
        logger.info { "Requesting metrics for $groupId/$appId to compare $headCommitSha with $mergeBaseCommitSha..." }
        val metrics = metricsClient.getBuildComparison(
            groupId = groupId,
            appId = appId,
            commitSha = headCommitSha,
            baselineCommitSha = mergeBaseCommitSha,
            useMaterializedViews = useMaterializedViews
        )
        val report = reportGenerator.getBuildComparisonReport(metrics)
        val mediaType: String = when (report.format) {
            ReportFormat.MARKDOWN -> "application/vnd.github.text+json"
            ReportFormat.PLAINTEXT -> "application/json"
        }
        logger.info { "Posting a comment to GitHub repository $githubRepository to pull request $githubPullRequestId..." }
        githubApiClient.postPullRequestComment(
            githubRepository,
            githubPullRequestId,
            report.content,
            mediaType
        )
    }

    suspend fun postPullRequestReportByEvent(
        githubEventFile: File,
        groupId: String,
        appId: String,
        useMaterializedViews: Boolean? = null
    ) {
        val json = Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
        val event = json.decodeFromString<GithubEvent>(githubEventFile.readText())
        val pullRequest = event.pullRequest.required("pullRequest")
        postPullRequestReport(
            githubRepository = event.repository.fullName,
            githubPullRequestId = pullRequest.number,
            groupId = groupId,
            appId = appId,
            headCommitSha = pullRequest.head.sha,
            mergeBaseCommitSha = gitClient.getMergeBaseCommitSha(targetRef = "origin/${pullRequest.base.ref}"),
            useMaterializedViews = useMaterializedViews
        )
    }

}