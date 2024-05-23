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

import com.epam.drill.integration.common.client.impl.DrillApiClientImpl
import com.epam.drill.integration.common.report.impl.TextReportGenerator
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillGitlabMergeRequestReportTask(ciCd: DrillCiCdProperties) {
    doFirst {
        val gitlab = ciCd.gitlab.required("drillCiCd.gitlab")

        val gitlabCiCdService = GitlabCiCdService(
            GitlabApiClientV4Impl(
                gitlab.gitlabApiUrl.required("drillCiCd.gitlab.gitlabApiUrl"),
                gitlab.gitlabPrivateToken
            ),
            DrillApiClientImpl(
                ciCd.drillApiUrl.required("drillCiCd.drillApiUrl"),
                ciCd.drillApiKey
            ),
            TextReportGenerator()
        )
        runBlocking {
            gitlabCiCdService.postMergeRequestReport(
                gitlabProjectId = gitlab.projectId.required("drillCiCd.gitlab.projectId"),
                gitlabMergeRequestId = gitlab.mergeRequestId.required("drillCiCd.gitlab.mergeRequestId"),
                groupId = ciCd.groupId.required("drillCiCd.groupId"),
                appId = ciCd.appId.required("drillCiCd.appId"),
                sourceBranch = ciCd.sourceBranch.required("drillCiCd.sourceBranch"),
                targetBranch = ciCd.targetBranch.required("drillCiCd.targetBranch"),
                commitSha = ciCd.latestCommitSha.required("drillCiCd.latestCommitSha")
            )
        }
    }
}