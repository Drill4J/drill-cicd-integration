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
import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.client.TestView
import com.epam.drill.integration.common.git.GitClient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

private const val RECOMMENDED_TESTS_LIMIT = 1000

class TestRecommendationService(
    private val metricsClient: MetricsClient,
    private val gitClient: GitClient,
    private val baselineFactory: BaselineFactory = BaselineFactory(gitClient, metricsClient)
) {
    private val logger = KotlinLogging.logger {}
    private val json = Json { prettyPrint = true }

    suspend fun getRecommendedTests(
        groupId: String,
        appId: String,
        buildVersion: String? = null,
        baselineSearchStrategy: BaselineSearchStrategy,
        baselineSearchCriteria: BaselineSearchCriteria,
        outputPath: String,
    ) {
        val commitSha = takeIf { buildVersion == null }?.let { gitClient.getCurrentCommitSha() }
        val baseline = baselineFactory.produce(baselineSearchStrategy).findBaseline(groupId, appId, baselineSearchCriteria)

        logger.info { "Requesting recommended tests to skip for $groupId/$appId, comparing ${buildVersion ?: commitSha} with $baseline..." }
        val tests: List<TestView> = metricsClient.getImpactedTests(
            groupId = groupId,
            appId = appId,
            buildVersion = buildVersion,
            commitSha = commitSha,
            baselineCommitSha = baseline.commitSha,
            baselineBuildVersion = baseline.buildVersion,
            testsToSkip = true,
            limit = RECOMMENDED_TESTS_LIMIT,
        )

        val directory = File(outputPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val outputFile = File(directory, "recommendedTests.json")
        val content = json.encodeToString(ListSerializer(TestView.serializer()), tests)
        outputFile.writeText(content)

        logger.info { "Found ${tests.size} test(s) that can be skipped. Saved to: ${outputFile.absolutePath}" }
    }
}

