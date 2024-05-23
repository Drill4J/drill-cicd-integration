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

import com.epam.drill.integration.common.client.BuildPayload
import com.epam.drill.integration.common.client.impl.DrillApiClientImpl
import com.epam.drill.integration.common.git.getGitBranch
import com.epam.drill.integration.common.git.getGitCommitInfo
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class PostBuildInfoCommand : CliktCommand(name = "postBuildInfo") {
    private val drillApiUrl by option("-drill-u", "--drillApiUrl", envvar = "DRILL_API_URL").required()
    private val drillApiKey by option("-drill-k", "--drillApiKey", envvar = "DRILL_API_KEY")
    private val drillGroupId by option("-g", "--groupId", envvar = "DRILL_GROUP_ID").required()
    private val drillAgentId by option("-a", "--appId", envvar = "DRILL_APP_ID").required()
    private val buildVersion by option("-v", "--buildVersion", envvar = "DRILL_BUILD_VERSION")

    override fun run() {
        echo("Posting Drill4J Build Info ...")
        val drillApiClient = DrillApiClientImpl(
            drillUrl = drillApiUrl,
            drillApiKey = drillApiKey
        )

        val branch = getGitBranch()
        val commitInfo = getGitCommitInfo()
        val payload = BuildPayload(
            groupId = drillGroupId,
            appId = drillAgentId,
            buildVersion = buildVersion,
            commitSha = commitInfo.commitSha,
            commitDate = commitInfo.commitDate,
            commitAuthor = commitInfo.commitAuthor,
            commitMessage = commitInfo.commitMessage,
            branch = branch
        )

        runBlocking {
            drillApiClient.postBuild(payload)
        }
        echo("Done.")
    }

}