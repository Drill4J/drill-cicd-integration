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


open class DrillProperties(
    var drillApiUrl: String? = null,
    var drillApiKey: String? = null,
    var groupId: String? = null,
    var appId: String? = null,
    var buildVersion: String? = null,
    var packagePrefixes : Array<String> = emptyArray(),

    var baseline: BaselineProperties? = null,
    var gitlab: DrillGitlabProperties? = null,
    var github: DrillGithubProperties? = null,

    var testAgent: TestAgentProperties? = null,
    var appAgent: AppAgentProperties? = null
) {
    fun baseline(configure: BaselineProperties.() -> Unit) {
        this.baseline = BaselineProperties().apply(configure)
    }
    fun gitlab(configure: DrillGitlabProperties.() -> Unit) {
        this.gitlab = DrillGitlabProperties().apply(configure)
    }
    fun github(configure: DrillGithubProperties.() -> Unit) {
        this.github = DrillGithubProperties().apply(configure)
    }
    fun enableTestAgent(configure: TestAgentProperties.() -> Unit) {
        this.testAgent = TestAgentProperties().apply(configure)
    }
    fun enableTestAgent() {
        this.testAgent = TestAgentProperties()
    }
    fun enableAppAgent(configure: AppAgentProperties.() -> Unit) {
        this.appAgent = AppAgentProperties().apply(configure)
    }
    fun enableAppAgent() {
        this.appAgent = AppAgentProperties()
    }
}

open class DrillGitlabProperties(
    var apiUrl: String? = null,
    var privateToken: String? = null,
    var projectId: String? = null,
    var commitSha: String? = null,
    var mergeRequest: MergeRequest = MergeRequest()
) {
    fun mergeRequest(configure: MergeRequest.() -> Unit) {
        this.mergeRequest = MergeRequest().apply(configure)
    }
}

open class MergeRequest(
    var mergeRequestIid: String? = null,
    var sourceBranch: String? = null,
    var targetBranch: String? = null,
    var mergeBaseCommitSha: String? = null,
)

open class DrillGithubProperties(
    var apiUrl: String = "https://api.github.com",
    var token: String? = null,
    var eventFilePath: String? = null
)

open class BaselineProperties(
    var searchStrategy: BaselineSearchStrategy? = null,
    var tagPattern: String? = null,
    var targetRef: String? = null,
)

abstract class AgentProperties(
    var version: String? = null,
    var downloadUrl: String? = null,
    var zipPath: String? = null,

    var logLevel: String? = null,
    var logFile: String? = null,
)

open class TestAgentProperties() : AgentProperties()

open class AppAgentProperties() : AgentProperties()