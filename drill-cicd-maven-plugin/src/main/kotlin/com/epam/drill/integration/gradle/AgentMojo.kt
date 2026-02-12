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

import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.baseline.BaselineFactory
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.util.required
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject

@Mojo(
    name = "enableAgent",
    defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true
)
class AgentMojo : AbstractAgentMojo() {

    @Parameter(property = "coverage", required = false)
    var coverage: CoverageConfiguration? = null

    @Parameter(property = "testTracking", required = false)
    var testTracking: TestTrackingConfiguration? = null

    @Parameter(property = "recommendedTests", required = false)
    var recommendedTests: RecommendedTestsConfiguration? = null

    override fun getAgentConfig() = AgentConfiguration().apply {
        val config = this@AgentMojo
        mapGeneralAgentProperties(config)
        mapBuildSpecificProperties(config, log, gitClient)
        mapClassScanningProperties(config, project, mojoExecution.lifecyclePhase, null)
        mapCoverageProperties(config)
        mapTestSpecificProperties(config, project, session, log, gitClient, baselineFactory)
    }
}

private fun generateTestTaskId(project: MavenProject, session: MavenSession?): String {
    return "${project.groupId}:${project.artifactId}:${getGoals(session)}${getProfiles(session)}"
}

private fun getProfiles(session: MavenSession?): String {
    val profiles = session?.request?.activeProfiles?.joinToString(";")
    return if (!profiles.isNullOrEmpty()) "($profiles)" else ""
}

private fun getGoals(session: MavenSession?) = session?.request?.goals?.joinToString(";")

internal fun AgentConfiguration.mapCoverageProperties(
    config: AgentMojo,
) {
    this.coverageCollectionEnabled = config.coverage?.enabled ?: false
}

internal fun AgentConfiguration.mapTestSpecificProperties(
    config: AgentMojo,
    project: MavenProject,
    session: MavenSession?,
    log: Log,
    gitClient: GitClient,
    baselineFactory: BaselineFactory,
) {
    this.testAgentEnabled = config.testTracking?.enabled ?: false
    this.testTaskId = config.testTaskId ?: generateTestTaskId(project, session)
    this.testTracingEnabled = (config.testTracking?.enabled ?: false) && (config.coverage?.perTestLaunch ?: false)
    this.testLaunchMetadataSendingEnabled = config.testTracking?.enabled ?: false
    this.recommendedTestsEnabled = config.recommendedTests?.enabled ?: false
    if (this.recommendedTestsEnabled == true) {
        this.recommendedTestsCoveragePeriodDays = config.recommendedTests?.coveragePeriodDays
        this.recommendedTestsTargetAppId = config.appId
        this.recommendedTestsTargetCommitSha = runCatching {
            gitClient.getCurrentCommitSha()
        }.onFailure {
            log.warn("Unable to retrieve the current commit SHA. The 'recommendedTestsTargetCommitSha' parameter will not be set. Error: ${it.message}")
        }.getOrNull()
        this.recommendedTestsTargetBuildVersion = config.buildVersion
        config.baseline?.let { baseline ->
            val searchStrategy = baseline.searchStrategy
            val baselineTagPattern = baseline.tagPattern ?: "*"
            val baselineTargetRef = baseline.targetRef
            if (searchStrategy != null) {
                val searchCriteria = when (searchStrategy) {
                    BaselineSearchStrategy.SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
                    BaselineSearchStrategy.SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baselineTargetRef"))
                }
                this.recommendedTestsBaselineCommitSha =
                    baselineFactory.produce(searchStrategy).findBaseline(searchCriteria)
            }
        }
    }
}