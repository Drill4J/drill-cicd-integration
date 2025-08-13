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
package com.epam.drill.integration.common.agent.config

class TestAgentConfiguration : AgentConfiguration() {
    var testTaskId: String? = null
    var labels: Map<String, String>? = null
    var recommendedTestsEnabled: Boolean? = null
    var recommendedTestsCoveragePeriodDays: Int? = null
    var recommendedTestsTargetAppId: String? = null
    var recommendedTestsTargetCommitSha: String? = null
    var recommendedTestsTargetBuildVersion: String? = null
    var recommendedTestsBaselineCommitSha: String? = null
    var recommendedTestsUseMaterializedViews: Boolean? = null

    override val githubRepository: String = "Drill4J/autotest-agent"
    override val agentName: String = "testAgent"


    override fun toAgentArguments(): MutableMap<String, String?> {
        return super.toAgentArguments().apply {
            testTaskId?.let { this[TestAgentConfiguration::testTaskId.name] = it }
            labels?.let {
                this[TestAgentConfiguration::labels.name] = it.map { (k, v) -> "$k:$v" }.joinToString(";")
            }
            recommendedTestsEnabled?.let { enabled ->
                this[TestAgentConfiguration::recommendedTestsEnabled.name] = enabled.toString().lowercase()
                recommendedTestsCoveragePeriodDays?.let { this[TestAgentConfiguration::recommendedTestsCoveragePeriodDays.name] = it.toString() }
                recommendedTestsCoveragePeriodDays?.let { this[TestAgentConfiguration::recommendedTestsCoveragePeriodDays.name] = it.toString() }
                recommendedTestsTargetAppId?.let { this[TestAgentConfiguration::recommendedTestsTargetAppId.name] = it }
                recommendedTestsTargetCommitSha?.let { this[TestAgentConfiguration::recommendedTestsTargetCommitSha.name] = it }
                recommendedTestsTargetBuildVersion?.let { this[TestAgentConfiguration::recommendedTestsTargetBuildVersion.name] = it }
                recommendedTestsBaselineCommitSha?.let { this[TestAgentConfiguration::recommendedTestsBaselineCommitSha.name] = it }
                recommendedTestsUseMaterializedViews?.let { this[TestAgentConfiguration::recommendedTestsUseMaterializedViews.name] = it.toString().lowercase() }
            }
        }
    }

}
