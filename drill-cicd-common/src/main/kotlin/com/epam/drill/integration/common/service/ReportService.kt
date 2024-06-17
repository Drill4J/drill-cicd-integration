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

import com.epam.drill.integration.common.baseline.*
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_TAG
import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.report.ReportFormat
import com.epam.drill.integration.common.report.ReportGenerator
import mu.KotlinLogging
import java.io.File

class ReportService(
    private val metricsClient: MetricsClient,
    private val gitClient: GitClient,
    private val reportGenerator: ReportGenerator,
    private val baselineFinders: (BaselineSearchStrategy) -> BaselineFinder<BaselineSearchCriteria> = { strategy ->
        when (strategy) {
            SEARCH_BY_TAG -> BaselineFinderByTag(gitClient) as BaselineFinder<BaselineSearchCriteria>
        }
    }
) {
    private val logger = KotlinLogging.logger {}

    suspend fun generateChangeTestingReport(
        groupId: String,
        appId: String,
        baselineSearchStrategy: BaselineSearchStrategy,
        baselineSearchCriteria: BaselineSearchCriteria,
        reportPath: String = ""
    ) {
        val commitSha = gitClient.getCurrentCommitSha()
        val baselineCommitSha = baselineFinders(baselineSearchStrategy).findBaseline(baselineSearchCriteria)

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
        val file = if (reportPath.isNotEmpty()) {
            val directory = File(reportPath)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            File(directory, fileName)
        } else
            File(fileName)
        logger.info { "Saving a report to the file ${file.absolutePath} ..." }
        file.writeText(report.content)
    }
}