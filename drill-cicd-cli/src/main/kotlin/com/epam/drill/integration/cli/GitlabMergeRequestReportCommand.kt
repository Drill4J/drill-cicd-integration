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
package com.epam.drill.integration.cli

import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class GitlabMergeRequestReportCommand : CliktCommand(name = "gitlabMergeRequestReport") {
    private val apiUrl by option("-drill-u", "--apiUrl", envvar = "DRILL_API_URL").required()
    private val apiKey by option("-drill-k", "--apiKey", envvar = "DRILL_API_KEY")
    private val groupId by option("-g", "--groupId", envvar = "DRILL_GROUP_ID").required()
    private val appId by option("-a", "--appId", envvar = "DRILL_APP_ID").required()
    private val commitSha by option("-c", "--commitSha", envvar = "CI_COMMIT_SHA").required()
    private val mergeBaseCommitSha by option("-mb", "--mergeBaseCommitSha", envvar = "CI_MERGE_REQUEST_DIFF_BASE_SHA").required()
    private val gitlabApiUrl by option("-gl-u", "--gitlabApiUrl", envvar = "GITLAB_API_URL").required()
    private val gitlabPrivateToken by option("-gl-t", "--gitlabPrivateToken", envvar = "GITLAB_PRIVATE_TOKEN").required()
    private val gitlabProjectId by option("-p", "--gitlabProjectId", envvar = "CI_PROJECT_ID").required()
    private val gitlabMergeRequestId by option("-mr", "--gitlabMergeRequestId", envvar = "CI_MERGE_REQUEST_IID").required()

    override fun run() {
        echo("Posting Drill4J Merge Request Report to Gitlab...")
        val gitlabCiCdService = GitlabCiCdService(
            GitlabApiClientV4Impl(gitlabApiUrl, gitlabPrivateToken),
            MetricsClientImpl(apiUrl, apiKey),
            MarkdownReportGenerator()
        )
        runBlocking {
            gitlabCiCdService.postMergeRequestReport(
                gitlabProjectId = gitlabProjectId,
                gitlabMergeRequestId = gitlabMergeRequestId,
                groupId = groupId,
                appId = appId,
                headCommitSha = commitSha,
                mergeBaseCommitSha = mergeBaseCommitSha
            )
        }
        echo("Done.")
    }
}