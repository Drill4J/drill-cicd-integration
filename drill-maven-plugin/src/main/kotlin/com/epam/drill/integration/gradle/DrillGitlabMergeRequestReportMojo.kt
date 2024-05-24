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
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.gitlab.client.impl.GitlabApiClientV4Impl
import com.epam.drill.integration.gitlab.service.GitlabCiCdService
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(
    name = "drillGitlabMergeRequestReport",
    defaultPhase = LifecyclePhase.INSTALL,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class DrillGitlabMergeRequestReportMojo : AbstractMojo() {

    @Parameter
    private lateinit var ciCd: DrillCiCdProperties

    override fun execute() {
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
                drillGroupId = ciCd.groupId.required("drillCiCd.groupId"),
                drillAgentId = ciCd.agentId.required("drillCiCd.agentId"),
                sourceBranch = ciCd.sourceBranch.required("drillCiCd.sourceBranch"),
                targetBranch = ciCd.targetBranch.required("drillCiCd.targetBranch"),
                latestCommitSha = ciCd.latestCommitSha.required("drillCiCd.latestCommitSha")
            )
        }
    }
}