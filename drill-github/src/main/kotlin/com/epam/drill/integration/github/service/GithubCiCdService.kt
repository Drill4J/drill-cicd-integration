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

import com.epam.drill.integration.common.metrics.DrillApiClient
import com.epam.drill.integration.common.report.ReportFormat
import com.epam.drill.integration.common.report.ReportGenerator
import com.epam.drill.integration.github.client.GithubApiClient
import com.epam.drill.integration.github.model.GithubEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.decodeFromStream
import java.io.File

class GithubCiCdService(
    private val githubApiClient: GithubApiClient,
    private val drillApiClient: DrillApiClient,
    private val reportGenerator: ReportGenerator
) {
    suspend fun postPullRequestReport(
        githubRepository: String,
        githubPullRequestId: Int,
        groupId: String,
        appId: String,
        sourceBranch: String,
        targetBranch: String,
        headCommitSha: String,
        mergeBaseCommitSha: String
    ) {
        val metrics = drillApiClient.getBuildComparison(
            groupId = groupId,
            appId = appId,
            commitSha = headCommitSha,
            baselineCommitSha = mergeBaseCommitSha
        )
        val comment = reportGenerator.getDiffSummaryReport(metrics)
        val mediaType: String = when (reportGenerator.getFormat()) {
            ReportFormat.MARKDOWN -> "application/vnd.github.text+json"
            ReportFormat.PLAINTEXT -> "application/json"
        }
        githubApiClient.postPullRequestReport(
            githubRepository,
            githubPullRequestId,
            comment,
            mediaType
        )
    }

    suspend fun postPullRequestReportByEvent(
        githubEventFile: File,
        groupId: String,
        appId: String
    ) {
        val json = Json {
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
        val event = json.decodeFromStream<GithubEvent>(githubEventFile.inputStream())
        postPullRequestReport(
            githubRepository = event.repository.fullName,
            githubPullRequestId = event.pullRequest.number,
            groupId = groupId,
            appId = appId,
            sourceBranch = event.pullRequest.head.ref,
            targetBranch = event.pullRequest.base.ref,
            headCommitSha = event.pullRequest.head.sha,
            mergeBaseCommitSha = event.pullRequest.mergeCommitSha
        )
    }

}