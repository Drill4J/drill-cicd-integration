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

import com.epam.drill.integration.common.metrics.BuildPayload
import com.epam.drill.integration.common.metrics.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.getGitBranch
import com.epam.drill.integration.common.git.getGitCommitInfo
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task

fun Task.drillSendBuildInfo(ciCd: DrillCiCdProperties) {
    doFirst {
        val drillApiClient = MetricsClientImpl(
            drillApiUrl = ciCd.drillApiUrl.required("drillApiUrl"),
            drillApiKey = ciCd.drillApiKey
        )

        val branch = getGitBranch()
        val commitInfo = getGitCommitInfo()
        val payload = BuildPayload(
            groupId = ciCd.groupId.required("groupId"),
            appId = ciCd.appId.required("appId"),
            buildVersion = ciCd.buildVersion,
            commitSha = commitInfo.sha,
            commitDate = commitInfo.date,
            commitAuthor = commitInfo.author,
            commitMessage = commitInfo.message,
            branch = branch
        )

        runBlocking {
            drillApiClient.sendBuild(payload)
        }
    }
}