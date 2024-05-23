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

import com.epam.drill.integration.common.client.DrillApiClient
import com.epam.drill.integration.common.report.ReportGenerator
import com.epam.drill.integration.gitlab.client.GitlabApiClient

class GitlabCiCdService(
    private val gitlabApiClient: GitlabApiClient,
    private val drillApiClient: DrillApiClient,
    private val reportGenerator: ReportGenerator
) {
    suspend fun postMergeRequestReport(
        gitlabProjectId: String,
        gitlabMergeRequestId: String,
        groupId: String,
        appId: String,
        sourceBranch: String,
        targetBranch: String,
        commitSha: String
    ) {
        val metrics = drillApiClient.getDiffMetricsByBranches(
            groupId,
            appId,
            sourceBranch,
            targetBranch,
            commitSha
        )
        val comment = reportGenerator.getDiffSummaryReport(
            metrics
        )
        gitlabApiClient.postMergeRequestReport(
            gitlabProjectId,
            gitlabMergeRequestId,
            comment
        )
    }

}