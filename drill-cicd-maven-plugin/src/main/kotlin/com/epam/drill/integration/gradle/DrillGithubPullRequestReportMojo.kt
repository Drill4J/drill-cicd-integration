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
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.github.client.impl.GithubApiClientImpl
import com.epam.drill.integration.github.service.GithubCiCdService
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(
    name = "drillGithubPullRequestReport",
    defaultPhase = LifecyclePhase.INSTALL,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class DrillGithubPullRequestReportMojo : AbstractMojo() {

    @Parameter(property = "drillApiUrl", required = true)
    var drillApiUrl: String? = null

    @Parameter(property = "drillApiKey")
    var drillApiKey: String? = null

    @Parameter(property = "groupId", required = true)
    var groupId: String? = null

    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "commitSha", required = true)
    var commitSha: String? = null

    @Parameter(property = "sourceBranch", required = true)
    var sourceBranch: String? = null

    @Parameter(property = "targetBranch", required = true)
    var targetBranch: String? = null

    @Parameter(property = "github", required = true)
    var github: DrillGithubProperties? = null

    override fun execute() {
        val github = github.required("github")

        val githubCiCdService = GithubCiCdService(
            GithubApiClientImpl(
                github.apiUrl,
                github.token.required("github.token"),
            ),
            DrillApiClientImpl(
                drillApiUrl.required("drillApiUrl"),
                drillApiKey
            ),
            MarkdownReportGenerator()
        )
        runBlocking {
            githubCiCdService.postPullRequestReport(
                githubRepository = github.repository.required("github.repository"),
                githubPullRequestId = github.pullRequestNumber.required("github.pullRequestNumber"),
                groupId = groupId.required("groupId"),
                appId = appId.required("appId"),
                sourceBranch = sourceBranch.required("sourceBranch"),
                targetBranch = targetBranch.required("targetBranch"),
                commitSha = commitSha.required("commitSha")
            )
        }
    }
}