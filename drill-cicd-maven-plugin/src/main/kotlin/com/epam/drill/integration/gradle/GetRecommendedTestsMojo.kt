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
import com.epam.drill.integration.common.baseline.BuildVersionCriteria
import com.epam.drill.integration.common.baseline.CommitCriteria
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.service.TestRecommendationService
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.ResolutionScope
import java.io.File


@Mojo(
    name = "getRecommendedTests",
    defaultPhase = LifecyclePhase.NONE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class GetRecommendedTestsMojo : AbstractAppDrillMojo() {

    override fun execute() {
        val gitClient = GitClientImpl()
        val metricsClient = MetricsClientImpl(
            apiUrl = apiUrl.fromEnv("DRILL_API_URL").required("apiUrl"),
            apiKey = apiKey.fromEnv("DRILL_API_KEY"),
        )
        val testRecommendationService = TestRecommendationService(
            metricsClient = metricsClient,
            gitClient = gitClient,
        )

        val groupId = groupId.fromEnv("DRILL_GROUP_ID").required("groupId")
        val appId = appId.fromEnv("DRILL_APP_ID").required("appId")
        val buildVersion = buildVersion.fromEnv("DRILL_BUILD_VERSION")
        println("!!! BuildVersion = $buildVersion")
        val baselineSearchStrategy = baseline?.searchStrategy ?: BaselineSearchStrategy.SEARCH_BY_TAG
        val baselineTagPattern = baseline?.tagPattern ?: "*"
        val baselineTargetRef = baseline?.targetRef
        val baselineCommitSha = baseline?.commitSha
        val baselineBuildVersion: String? = baseline?.buildVersion

        val searchCriteria = when (baselineSearchStrategy) {
            BaselineSearchStrategy.SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
            BaselineSearchStrategy.SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baseline.targetRef"))
            BaselineSearchStrategy.SEARCH_BY_COMMIT -> CommitCriteria(baselineCommitSha.required("baseline.commitSha"))
            BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION -> BuildVersionCriteria(baselineBuildVersion.required("baseline.buildVersion"))
        }

        log.info("Getting Drill4J Recommended Tests to skip...")
        val outputPath = File(project.build?.directory, "drill").absolutePath
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
        log.info("Drill4J Recommended Tests saved to: ${outputFile.absolutePath}")
    }
}

