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
package com.epam.drill.integration.gitlab.service

import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.report.ReportGenerator
import com.epam.drill.integration.gitlab.client.GitlabApiClient
import mu.KotlinLogging

class GitlabCiCdService(
    private val gitlabApiClient: GitlabApiClient,
    private val metricsClient: MetricsClient,
    private val reportGenerator: ReportGenerator
) {
    private val logger = KotlinLogging.logger { }

    suspend fun postMergeRequestReport(
        gitlabProjectId: String,
        gitlabMergeRequestId: String,
        groupId: String,
        appId: String,
        sourceBranch: String,
        targetBranch: String,
        headCommitSha: String,
        mergeBaseCommitSha: String
    ) {
        logger.info { "Requesting metrics for $groupId/$appId to compare $headCommitSha with $mergeBaseCommitSha..." }
        val metrics = metricsClient.getBuildComparison(
            groupId,
            appId,
            sourceBranch,
            targetBranch,
            headCommitSha,
            mergeBaseCommitSha
        )
        val report = reportGenerator.getBuildComparisonReport(
            metrics
        )
        logger.info { "Posting a comment to Gitlab project $gitlabProjectId to merge request $gitlabMergeRequestId..." }
        gitlabApiClient.postMergeRequestComment(
            gitlabProjectId,
            gitlabMergeRequestId,
            report.content
        )
    }

}