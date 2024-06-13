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
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.github.client.impl.GithubApiClientImpl
import com.epam.drill.integration.github.service.GithubCiCdService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import java.io.File

class GithubPullRequestReportByEventCommand: CliktCommand(name = "githubPullRequestReportByEvent") {
    private val drillApiUrl by option("-drill-u", "--drillApiUrl", envvar = "INPUT_DRILL_API_URL").required()
    private val drillApiKey by option("-drill-k", "--drillApiKey", envvar = "INPUT_DRILL_API_KEY")
    private val groupId by option("-g", "--groupId", envvar = "INPUT_GROUP_ID").required()
    private val appId by option("-a", "--appId", envvar = "INPUT_APP_ID").required()
    private val githubApiUrl by option("-gh-u", "--githubApiUrl", envvar = "GITHUB_API_URL").default("https://api.github.com")
    private val githubToken by option("-gh-t", "--githubToken", envvar = "INPUT_GITHUB_TOKEN").required()
    private val eventFilePath by option("-ef", "--eventFilePath", envvar = "GITHUB_EVENT_PATH").required()

    override fun run() {
        echo("Posting Drill4J Pull Request Report to GitHub by GitHub Event...")
        val githubCiCdService = GithubCiCdService(
            GithubApiClientImpl(githubApiUrl, githubToken),
            MetricsClientImpl(drillApiUrl, drillApiKey),
            MarkdownReportGenerator(),
            GitClientImpl()
        )
        runBlocking {
            githubCiCdService.postPullRequestReportByEvent(
                File(eventFilePath),
                groupId,
                appId
            )
        }
        echo("Done.")
    }
}