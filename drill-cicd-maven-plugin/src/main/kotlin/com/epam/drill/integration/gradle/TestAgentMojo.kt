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

import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import com.epam.drill.integration.common.baseline.BaselineFactory
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.required
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugins.annotations.*

@Mojo(
    name = "enableTestAgent",
    defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true
)
class TestAgentMojo : AbstractAgentMojo() {
    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "buildVersion", required = false)
    var buildVersion: String? = null

    @Parameter(property = "testAgent", required = true)
    var testAgent: TestAgentMavenConfiguration? = null

    @Parameter(property = "baseline", required = true)
    var baseline: BaselineConfiguration? = null

    @Parameter(property = "testRecommendations")
    var testRecommendations: RecommendedTestsConfiguration? = null

    @Component
    var session: MavenSession? = null

    private val gitClient = GitClientImpl()
    private val baselineFactory = BaselineFactory(gitClient)

    override fun getAgentConfig() = TestAgentConfiguration().apply {
        val mavenConfig = this@TestAgentMojo
        val testAgent = mavenConfig.testAgent.required("testAgent")

        setGeneralAgentProperties(testAgent, mavenConfig)
        testTaskId = testAgent.testTaskId ?: generateTestTaskId()
        recommendedTestsEnabled = mavenConfig.testRecommendations?.enabled ?: false
        mavenConfig.testRecommendations.takeIf { recommendedTestsEnabled == true }?.let { recommendedTests ->
            recommendedTestsCoveragePeriodDays = recommendedTests.coveragePeriodDays
            recommendedTestsTargetAppId = mavenConfig.appId
            recommendedTestsTargetCommitSha = runCatching {
                gitClient.getCurrentCommitSha()
            }.onFailure {
                log.warn("Unable to retrieve the current commit SHA. The 'recommendedTestsTargetCommitSha' parameter will not be set. Error: ${it.message}")
            }.getOrNull()
            recommendedTestsTargetBuildVersion = mavenConfig.buildVersion
            mavenConfig.baseline?.takeIf { it.searchStrategy != null }?.let { baseline ->
                val baselineTagPattern = baseline.tagPattern ?: "*"
                val baselineTargetRef = baseline.targetRef
                baseline.searchStrategy?.let { searchStrategy ->
                    val searchCriteria = when (searchStrategy) {
                        BaselineSearchStrategy.SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
                        BaselineSearchStrategy.SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baselineTargetRef"))
                    }
                    recommendedTestsBaselineCommitSha = baselineFactory.produce(searchStrategy).findBaseline(searchCriteria)
                }
            }
        }
        log.info("testTaskId: $testTaskId")
    }

    private fun generateTestTaskId(): String {
        return "${project.groupId}:${project.artifactId}:${getGoals()}${getProfiles()}"
    }

    private fun getProfiles(): String {
        val profiles = session?.request?.activeProfiles?.joinToString(";")
        return if (!profiles.isNullOrEmpty()) "($profiles)" else ""
    }

    private fun getGoals() = session?.request?.goals?.joinToString(";")
}

