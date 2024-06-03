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

import com.epam.drill.integration.common.client.impl.DrillApiClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.github.client.impl.GithubApiClientImpl
import com.epam.drill.integration.github.service.GithubCiCdService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking

class GithubPullRequestReportCommand: CliktCommand(name = "githubPullRequestReport") {
    private val drillApiUrl by option("-drill-u", "--drillApiUrl", envvar = "INPUT_DRILL_API_URL").required()
    private val drillApiKey by option("-drill-k", "--drillApiKey", envvar = "INPUT_DRILL_API_KEY")
    private val drillGroupId by option("-g", "--groupId", envvar = "INPUT_GROUP_ID").required()
    private val drillAppId by option("-a", "--appId", envvar = "INPUT_APP_ID").required()
    private val sourceBranch by option("-sb", "--sourceBranch", envvar = "GITHUB_HEAD_REF").required()
    private val targetBranch by option("-tb", "--targetBranch", envvar = "GITHUB_BASE_REF").required()
    private val commitSha by option("-c", "--commitSha", envvar = "GITHUB_SHA").required()
    private val githubApiUrl by option("-gh-u", "--githubApiUrl", envvar = "GITHUB_API_URL").default("https://api.github.com")
    private val githubToken by option("-gh-t", "--githubToken", envvar = "INPUT_GITHUB_TOKEN").required()
    private val githubRepository by option("-r", "--githubRepository", envvar = "GITHUB_REPOSITORY").required()
    private val githubPullRequestNumber by option("-pr", "--githubPullRequestNumber", envvar = "INPUT_PULL_REQUEST_NUMBER").int().required()

    override fun run() {
        echo("Posting Drill4J Pull Request Report to GitHub...")
        val githubCiCdService = GithubCiCdService(
            GithubApiClientImpl(githubApiUrl, githubToken),
            DrillApiClientImpl(drillApiUrl, drillApiKey),
            MarkdownReportGenerator()
        )
        runBlocking {
            githubCiCdService.postPullRequestReport(
                githubRepository,
                githubPullRequestNumber,
                drillGroupId,
                drillAppId,
                sourceBranch,
                targetBranch,
                commitSha
            )
        }
        echo("Done.")
    }
}