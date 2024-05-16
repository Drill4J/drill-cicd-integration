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
import com.epam.drill.integration.common.model.MediaType.GITLAB_JSON
import com.epam.drill.integration.common.report.impl.TextReportGenerator
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillGitlabMergeRequestReportTask(ciCd: DrillCiCdProperties) {
    doFirst {
        val gitlabCiCdService = GitlabCiCdService(
            GitlabApiClientV4Impl(ciCd.gitlab.gitlabApiUrl!!, ciCd.gitlab.gitlabPrivateToken),
            DrillApiClientImpl(ciCd.drillApiUrl!!, ciCd.drillApiKey),
            TextReportGenerator(GITLAB_JSON)
        )
        runBlocking {
            gitlabCiCdService.postMergeRequestReport(
                gitlabProjectId = ciCd.gitlab.projectId!!,
                gitlabMergeRequestId = ciCd.gitlab.mergeRequestId!!,
                drillGroupId = ciCd.groupId!!,
                drillAgentId = ciCd.agentId!!,
                sourceBranch = ciCd.sourceBranch!!,
                targetBranch = ciCd.targetBranch!!,
                latestCommitSha = ciCd.latestCommitSha!!)
        }
    }
}