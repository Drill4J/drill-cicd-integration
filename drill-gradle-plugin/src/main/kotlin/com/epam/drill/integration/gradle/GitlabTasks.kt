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
import com.epam.drill.integration.common.report.impl.TextReportGenerator
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillGitlabMergeRequestReportTask(ciCd: DrillCiCdProperties) {
    doFirst {
        val gitlab = ciCd.gitlab.required("gitlab")
        val gitlabApiUrl = gitlab.apiUrl.required("gitlab.apiUrl")
        val gitlabPrivateToken = gitlab.privateToken
        val drillApiUrl = ciCd.drillApiUrl.required("drillApiUrl")
        val drillApiKey = ciCd.drillApiKey
        val gitlabProjectId = gitlab.projectId.required("gitlab.projectId")
        val gitlabMergeRequestId = gitlab.mergeRequestId.required("gitlab.mergeRequestId")
        val groupId = ciCd.groupId.required("groupId")
        val appId = ciCd.appId.required("appId")
        val sourceBranch = gitlab.mergeRequest.sourceBranch
            .fromEnv("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME")
            .required("gitlab.mergeRequest.sourceBranch")
        val targetBranch = gitlab.mergeRequest.targetBranch
            .fromEnv("CI_MERGE_REQUEST_TARGET_BRANCH_NAME")
            .required("gitlab.mergeRequest.sourceBranch")
        val commitSha = gitlab.mergeRequest.commitSha
            .fromEnv("CI_COMMIT_SHA")
            .required("gitlab.mergeRequest.commitSha")
        val mergeBaseCommitSha = gitlab.mergeRequest.mergeBaseCommitSha
            .fromEnv("CI_MERGE_REQUEST_DIFF_BASE_SHA")
            .required("gitlab.mergeRequest.mergeBaseCommitSha")

        val gitlabCiCdService = GitlabCiCdService(
            GitlabApiClientV4Impl(
                gitlabApiUrl,
                gitlabPrivateToken
            ),
            MetricsClientImpl(
                drillApiUrl,
                drillApiKey
            ),
            TextReportGenerator()
        )
        runBlocking {
            gitlabCiCdService.postMergeRequestReport(
                gitlabProjectId = gitlabProjectId,
                gitlabMergeRequestId = gitlabMergeRequestId,
                groupId = groupId,
                appId = appId,
                sourceBranch = sourceBranch,
                targetBranch = targetBranch,
                commitSha = commitSha,
                mergeBaseCommitSha = mergeBaseCommitSha,
            )
        }
    }
}