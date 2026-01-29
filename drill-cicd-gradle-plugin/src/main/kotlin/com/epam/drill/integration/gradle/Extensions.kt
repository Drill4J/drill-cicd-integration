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
import org.gradle.api.file.FileCollection

open class DrillPluginExtension(
    var apiUrl: String? = null,
    var apiKey: String? = null,
    var groupId: String? = null,
    var appId: String? = null,
    var buildVersion: String? = null,
    var packagePrefixes: Array<String> = emptyArray(),

    var envId: String? = null,
    var testTaskId: String? = null,

    var agent: AgentExtension = AgentExtension(),
    var coverage: CoverageExtension = CoverageExtension(),
    var classScanning: ClassScanningExtension = ClassScanningExtension(),
    var testTracking: TestTracking = TestTracking(),

    var baseline: BaselineExtension = BaselineExtension(),
    var gitlab: GitlabExtension = GitlabExtension(),
    var github: GithubExtension = GithubExtension(),
    var recommendedTests: RecommendedTestsExtension = RecommendedTestsExtension(),
    var additionalParams: Map<String, String> = mutableMapOf()
) {
    fun agent(action: Action<AgentExtension>) {
        action.execute(agent)
    }

    fun coverage() {
        coverage.enabled = true
    }

    fun coverage(action: Action<CoverageExtension>) {
        coverage.enabled = true
        action.execute(coverage)
    }

    fun classScanning() {
        classScanning.enabled = true
    }

    fun classScanning(action: Action<ClassScanningExtension>) {
        classScanning.enabled = true
        action.execute(classScanning)
    }

    fun testTracking() {
        testTracking.enabled = true
    }

    fun testTracking(action: Action<TestTracking>) {
        testTracking.enabled = true
        action.execute(testTracking)
    }

    fun baseline(action: Action<BaselineExtension>) {
        action.execute(baseline)
    }

    fun gitlab(action: Action<GitlabExtension>) {
        action.execute(gitlab)
    }

    fun github(action: Action<GithubExtension>) {
        action.execute(github)
    }

    fun recommendedTests() {
        recommendedTests.enabled = true
    }

    fun recommendedTests(action: Action<RecommendedTestsExtension>) {
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
)

open class AgentExtension(
    var version: String? = null,
    var downloadUrl: String? = null,
    var zipPath: String? = null,

    var agentMode: String? = null,

    var logLevel: String? = null,
    var logFile: String? = null,
)

open class CoverageExtension(
    var enabled: Boolean = false,
    var perTestSession: Boolean = true,
    var perTestLaunch: Boolean = true,
)

open class ClassScanningExtension(
    var enabled: Boolean = false,
    var appClasses: FileCollection? = null,
    var testClasses: FileCollection? = null,
    var afterBuild: Boolean = false,
    var beforeTests: Boolean = true,
    var beforeRun: Boolean = true,
    var runtime: Boolean = false,
    var classLoaders: ClassLoaderScanningExtension = ClassLoaderScanningExtension()
)

open class ClassLoaderScanningExtension(
    var enabled: Boolean = false,
    var delay: Long = 5000L,
)

open class TestTracking(
    var enabled: Boolean = false,
)