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

import com.epam.drill.integration.common.metrics.impl.MetricsClientImpl
import com.epam.drill.integration.common.report.impl.TextReportGenerator
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillGitlabMergeRequestReportTask(ciCd: DrillCiCdProperties) {
    doFirst {
        val gitlab = ciCd.gitlab.required("gitlab")

        val gitlabCiCdService = GitlabCiCdService(
            GitlabApiClientV4Impl(
                gitlab.apiUrl.required("gitlab.apiUrl"),
                gitlab.privateToken
            ),
            MetricsClientImpl(
                ciCd.drillApiUrl.required("drillApiUrl"),
                ciCd.drillApiKey
            ),
            TextReportGenerator()
        )
        runBlocking {
            gitlabCiCdService.postMergeRequestReport(
                gitlabProjectId = gitlab.projectId.required("gitlab.projectId"),
                gitlabMergeRequestId = gitlab.mergeRequestId.required("gitlab.mergeRequestId"),
                groupId = ciCd.groupId.required("groupId"),
                appId = ciCd.appId.required("appId"),
                sourceBranch = ciCd.sourceBranch.required("sourceBranch"),
                targetBranch = ciCd.targetBranch.required("targetBranch"),
                commitSha = ciCd.commitSha.required("commitSha"),
                mergeBaseCommitSha = ciCd.mergeBaseCommitSha.required("mergeBaseCommitSha"),
            )
        }
    }
}