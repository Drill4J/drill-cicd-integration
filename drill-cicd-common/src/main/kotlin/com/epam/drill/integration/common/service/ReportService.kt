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
package com.epam.drill.integration.common.service

import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.GitException
import com.epam.drill.integration.common.report.ReportFormat
import com.epam.drill.integration.common.report.ReportGenerator
import mu.KotlinLogging
import java.io.File

class ReportService(
    private val metricsClient: MetricsClient,
    private val gitClient: GitClient,
    private val reportGenerator: ReportGenerator
) {
    private val logger = KotlinLogging.logger {}

    suspend fun generateChangeTestingReportByTag(
        groupId: String,
        appId: String,
        tagPattern: String
    ) {
        val commitSha = gitClient.getCurrentCommitSha()
        val baselineCommitSha = try {
            gitClient.findCommitShaByTagPattern(tagPattern)
        } catch (e: GitException) {
            if (e.exitCode == 128)
                throw IllegalStateException("No git tags found matching template $tagPattern", e)
            else
                throw e
        }
        logger.info { "Requesting metrics for $groupId/$appId to compare $commitSha with $baselineCommitSha..." }
        val data = metricsClient.getBuildComparison(
            groupId = groupId,
            appId = appId,
            commitSha = commitSha,
            baselineCommitSha = baselineCommitSha
        )
        val report = reportGenerator.getBuildComparisonReport(data)
        val fileExt = when (report.format) {
            ReportFormat.MARKDOWN -> "md"
            ReportFormat.PLAINTEXT -> "txt"
        }
        val fileName = "drillReport.$fileExt"
        logger.info { "Saving a report to the file $fileName..." }
        val file = File(fileName)
        file.writeText(report.content)
    }
}