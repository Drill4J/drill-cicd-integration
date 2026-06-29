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

import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_MERGE_BASE
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_TAG
import com.epam.drill.integration.common.baseline.BuildVersionCriteria
import com.epam.drill.integration.common.baseline.CommitCriteria
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.baseline.TagMatchBy
import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.service.ReportService
import com.epam.drill.integration.common.service.TestRecommendationService
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task
import java.io.File

fun Task.drillGenerateChangeTestingReport(config: DrillPluginExtension) {
    doFirst {
        val apiUrl = config.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
        val apiKey = config.apiKey.fromEnv("DRILL_API_KEY")
        val groupId = config.groupId.fromEnv("DRILL_GROUP_ID").required("groupId")
        val appId = config.appId.fromEnv("DRILL_APP_ID").required("appId")
        val buildVersion = config.buildVersion.fromEnv("DRILL_BUILD_VERSION")
        val baselineSearchStrategy = config.baseline.searchStrategy ?: SEARCH_BY_TAG
        val baselineTagPattern = config.baseline.tagPattern ?: "*"
        val baselineTargetRef = config.baseline.targetRef
        val baselineCommitSha = config.baseline.commitSha
        val baselineBuildVersion: String? = config.baseline.buildVersion

        val reportService = ReportService(
            metricsClient = MetricsClientImpl(
                apiUrl = apiUrl,
                apiKey = apiKey
            ),
            gitClient = GitClientImpl(),
            reportGenerator = MarkdownReportGenerator()
        )
        val searchCriteria = when (baselineSearchStrategy) {
            SEARCH_BY_TAG -> TagCriteria(
                tagPattern = baselineTagPattern,
                matchBy = config.baseline.tagMatchBy
                    ?.let { TagMatchBy.valueOf(it) }
                    ?: TagMatchBy.COMMIT_SHA,
                tagPrefix = config.baseline.tagPrefix ?: "",
            )
            SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baseline.targetRef"))
            BaselineSearchStrategy.SEARCH_BY_COMMIT -> CommitCriteria(baselineCommitSha.required("baseline.commitSha"))
            BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION -> BuildVersionCriteria(baselineBuildVersion.required("baseline.buildVersion"))
        }

        logger.lifecycle("Generating Drill4J Change Testing Report...")
        val reportPath = File(project.buildDir, "/drill-reports").absolutePath
        runBlocking {
            reportService.generateChangeTestingReport(
                groupId = groupId,
                appId = appId,
                buildVersion = buildVersion,
                baselineSearchStrategy = baselineSearchStrategy,
                baselineSearchCriteria = searchCriteria,
                reportPath = reportPath,
            )
        }
        logger.lifecycle("Drill4J Change Testing Report generated at: $reportPath")
    }
}

fun Task.drillGetRecommendedTests(config: DrillPluginExtension) {
    doFirst {
        val apiUrl = config.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
        val apiKey = config.apiKey.fromEnv("DRILL_API_KEY")
        val groupId = config.groupId.required("groupId")
        val appId = config.appId.required("appId")
        val buildVersion = config.buildVersion
        val baselineSearchStrategy = config.baseline.searchStrategy ?: SEARCH_BY_TAG
        val baselineTagPattern = config.baseline.tagPattern ?: "*"
        val baselineTargetRef = config.baseline.targetRef
        val baselineCommitSha = config.baseline.commitSha
        val baselineBuildVersion: String? = config.baseline.buildVersion

        val gitClient = GitClientImpl()
        val testRecommendationService = TestRecommendationService(
            metricsClient = MetricsClientImpl(
                apiUrl = apiUrl,
                apiKey = apiKey
            ),
            gitClient = gitClient,
        )
        val searchCriteria = when (baselineSearchStrategy) {
            SEARCH_BY_TAG -> TagCriteria(
                tagPattern = baselineTagPattern,
                matchBy = config.baseline.tagMatchBy
                    ?.let { TagMatchBy.valueOf(it) }
                    ?: TagMatchBy.COMMIT_SHA,
                tagPrefix = config.baseline.tagPrefix ?: "",
            )
            SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baseline.targetRef"))
            BaselineSearchStrategy.SEARCH_BY_COMMIT -> CommitCriteria(baselineCommitSha.required("baseline.commitSha"))
            BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION -> BuildVersionCriteria(baselineBuildVersion.required("baseline.buildVersion"))
        }

        logger.lifecycle("Getting Drill4J Recommended Tests to skip...")
        val outputPath = File(project.buildDir, "drill").absolutePath
        runBlocking {
            testRecommendationService.getRecommendedTests(
                groupId = groupId,
                appId = appId,
                buildVersion = buildVersion,
                baselineSearchStrategy = baselineSearchStrategy,
                baselineSearchCriteria = searchCriteria,
                outputPath = outputPath,
            )
        }
        val outputFile = File(outputPath, "recommendedTests.json")
        logger.lifecycle("Drill4J Recommended Tests saved to: ${outputFile.absolutePath}")
    }
}
