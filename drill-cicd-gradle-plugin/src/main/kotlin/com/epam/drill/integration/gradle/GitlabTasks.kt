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
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillGitlabMergeRequestReportTask(config: DrillPluginExtension) {
    doFirst {
        val gitlab = config.gitlab.required("gitlab")
        val gitlabApiUrl = gitlab.apiUrl.required("gitlab.apiUrl")
        val gitlabPrivateToken = gitlab.privateToken
        val apiUrl = config.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
        val apiKey = config.apiKey.fromEnv("DRILL_API_KEY")
        val groupId = config.groupId.required("groupId")
        val appId = config.appId.required("appId")
        val gitlabProjectId = gitlab.projectId.required("gitlab.projectId")
        val commitSha = gitlab.commitSha
            .fromEnv("CI_COMMIT_SHA")
            .required("gitlab.commitSha")
        val gitlabMergeRequestIid = gitlab.mergeRequest.mergeRequestIid
            .fromEnv("CI_MERGE_REQUEST_IID")
            .required("gitlab.mergeRequest.mergeRequestIid")
        val mergeBaseCommitSha = gitlab.mergeRequest.mergeBaseCommitSha
            .fromEnv("CI_MERGE_REQUEST_DIFF_BASE_SHA")
            .required("gitlab.mergeRequest.mergeBaseCommitSha")
        val useMaterializedViews = config.useMaterializedViews
            .fromEnv("DRILL_USE_MATERIALIZED_VIEWS")

        val gitlabCiCdService = GitlabCiCdService(
            GitlabApiClientV4Impl(
                gitlabApiUrl,
                gitlabPrivateToken
            ),
            MetricsClientImpl(
                apiUrl,
                apiKey
            ),
            MarkdownReportGenerator()
        )
        logger.lifecycle("Posting Drill4J Testing Report for $groupId/$appId to Gitlab project $gitlabProjectId to merge request $gitlabMergeRequestIid...")
        runBlocking {
            gitlabCiCdService.postMergeRequestReport(
                gitlabProjectId = gitlabProjectId,
                gitlabMergeRequestId = gitlabMergeRequestIid,
                groupId = groupId,
                appId = appId,
                headCommitSha = commitSha,
                mergeBaseCommitSha = mergeBaseCommitSha,
                useMaterializedViews = useMaterializedViews?.let { java.lang.Boolean.parseBoolean(it) }
            )
        }
    }
}