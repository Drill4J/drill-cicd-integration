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

import com.epam.drill.integration.common.client.BuildPayload
import com.epam.drill.integration.common.client.impl.DataIngestClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillSendBuildInfo(config: DrillPluginExtension) {
    doFirst {
        val apiUrl = config.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
        val apiKey = config.apiKey.fromEnv("DRILL_API_KEY")
        val groupId = config.groupId.required("groupId")
        val appId = config.appId.required("appId")
        val buildVersion = config.buildVersion.fromEnv("DRILL_BUILD_VERSION")

        val dataIngestClient = DataIngestClientImpl(
            apiUrl = apiUrl,
            apiKey = apiKey
        )
        val gitClient = GitClientImpl()

        val branch = gitClient.getGitBranch()
        val commitInfo = gitClient.getGitCommitInfo()
        val payload = BuildPayload(
            groupId = groupId,
            appId = appId,
            buildVersion = buildVersion,
            commitSha = commitInfo.sha,
            commitDate = commitInfo.date,
            commitAuthor = commitInfo.author,
            commitMessage = commitInfo.message,
            branch = branch
        )
        logger.lifecycle("Sending the current build information to Drill4J for $groupId/$appId...")
        runBlocking {
            dataIngestClient.sendBuild(payload)
        }
    }
}