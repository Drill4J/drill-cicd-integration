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

import com.epam.drill.integration.common.metrics.impl.MetricsClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.util.required
import com.epam.drill.integration.github.client.impl.GithubApiClientImpl
import com.epam.drill.integration.github.service.GithubCiCdService
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task
import java.io.File


fun Task.drillGithubPullRequestReport(ciCd: DrillCiCdProperties) {
    doFirst {
        val github = ciCd.github.required("drillCiCd.github")

        val githubCiCdService = GithubCiCdService(
            GithubApiClientImpl(
                github.apiUrl,
                github.token.required("drillCiCd.github.token"),
            ),
            MetricsClientImpl(
                ciCd.drillApiUrl.required("drillCiCd.drillApiUrl"),
                ciCd.drillApiKey
            ),
            MarkdownReportGenerator()
        )
        runBlocking {
            githubCiCdService.postPullRequestReportByEvent(
                groupId = ciCd.groupId.required("drillCiCd.groupId"),
                appId = ciCd.appId.required("drillCiCd.appId"),
                githubEventFile = File(github.eventFilePath.required("drillCiCd.github.eventFilePath")),
            )
        }
    }
}