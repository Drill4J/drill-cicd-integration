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
import org.gradle.api.Action

open class DrillPluginExtension(
    var apiUrl: String? = null,
    var apiKey: String? = null,
    var groupId: String? = null,
    var appId: String? = null,
    var buildVersion: String? = null,
    var packagePrefixes: Array<String> = emptyArray(),

    var baseline: BaselineExtension = BaselineExtension(),
    var gitlab: GitlabExtension = GitlabExtension(),
    var github: GithubExtension = GithubExtension(),
    var recommendedTests: RecommendedTestsExtension = RecommendedTestsExtension(),

    var useMaterializedViews: Boolean? = null,

    internal var testAgent: TestAgentExtension = TestAgentExtension(),
    internal var appAgent: AppAgentExtension = AppAgentExtension(),
) {
    fun baseline(action: Action<BaselineExtension>) {
        action.execute(baseline)
    }

    fun gitlab(action: Action<GitlabExtension>) {
        action.execute(gitlab)
    }

    fun github(action: Action<GithubExtension>) {
        action.execute(github)
    }

    fun enableTestAgent(action: Action<TestAgentExtension>) {
        testAgent.enabled = true
        action.execute(testAgent)
    }

    fun enableAppAgent(action: Action<AppAgentExtension>) {
        appAgent.enabled = true
        action.execute(appAgent)
    }

    fun enableTestRecommendations(action: Action<RecommendedTestsExtension>) {
        recommendedTests.enabled = true
        action.execute(recommendedTests)
    }
}

open class GitlabExtension(
    var apiUrl: String? = null,
    var privateToken: String? = null,
    var projectId: String? = null,
    var commitSha: String? = null,
    var mergeRequest: MergeRequestExtension = MergeRequestExtension()
) {
    fun mergeRequest(action: Action<MergeRequestExtension>) {
        action.execute(mergeRequest)
    }
}

open class MergeRequestExtension(
    var mergeRequestIid: String? = null,
    var sourceBranch: String? = null,
    var targetBranch: String? = null,
    var mergeBaseCommitSha: String? = null,
)

open class GithubExtension(
    var apiUrl: String = "https://api.github.com",
    var token: String? = null,
    var eventFilePath: String? = null
)

open class BaselineExtension(
    var searchStrategy: BaselineSearchStrategy? = null,
    var tagPattern: String? = null,
    var targetRef: String? = null,
)

open class RecommendedTestsExtension(
    var enabled: Boolean? = null,
    var coveragePeriodDays: Int? = null,
    var useMaterializedViews: Boolean? = null,
)

open class AgentExtension(
    var enabled: Boolean? = null,

    var version: String? = null,
    var downloadUrl: String? = null,
    var zipPath: String? = null,

    var logLevel: String? = null,
    var logFile: String? = null,

    var additionalParams: Map<String, String> = mutableMapOf()
)

open class TestAgentExtension(
    var testTaskId: String? = null
) : AgentExtension()

open class AppAgentExtension(
    var envId: String? = null
) : AgentExtension()

open class DrillTaskExtension(
    var testAgent: TestAgentExtension = TestAgentExtension(),
    var appAgent: AppAgentExtension = AppAgentExtension(),
) {
    fun testAgent(action: Action<TestAgentExtension>) {
        testAgent.enabled = true
        action.execute(testAgent)
    }

    fun appAgent(action: Action<AppAgentExtension>) {
        appAgent.enabled = true
        action.execute(appAgent)
    }
}