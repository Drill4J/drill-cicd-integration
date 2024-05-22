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
package com.epam.drill.coverage.maven

import com.epam.drill.integration.common.client.impl.DrillApiClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
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

    @Parameter
    private lateinit var drillCiCd: DrillCiCdProperties

    override fun execute() {
        val github = drillCiCd.github!!

        val githubCiCdService = GithubCiCdService(
            GithubApiClientImpl(
                github.githubApiUrl,
                github.githubToken!!,
            ),
            DrillApiClientImpl(
                drillCiCd.drillApiUrl!!,
                drillCiCd.drillApiKey
            ),
            MarkdownReportGenerator()
        )

        runBlocking {
            githubCiCdService.postPullRequestReport(
                githubRepository = github.githubRepository!!,
                githubPullRequestId = github.pullRequestId!!,
                drillGroupId = drillCiCd.groupId!!,
                drillAgentId = drillCiCd.agentId!!,
                sourceBranch = drillCiCd.sourceBranch!!,
                targetBranch = drillCiCd.targetBranch!!,
                latestCommitSha = drillCiCd.latestCommitSha!!
            )
        }
    }
}