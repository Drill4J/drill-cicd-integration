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

import com.epam.drill.integration.common.agent.config.AgentMode
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy

open class GitlabConfiguration(
    var apiUrl: String? = null,
    var privateToken: String? = null,
    var projectId: String? = null,
    var commitSha: String? = null,
    var mergeRequest: MergeRequestConfiguration = MergeRequestConfiguration()
)

open class MergeRequestConfiguration(
    var mergeRequestIid: String? = null,
    var mergeBaseCommitSha: String? = null,
    var sourceBranch: String? = null,
    var targetBranch: String? = null,
)

open class GithubConfiguration(
    var apiUrl: String = "https://api.github.com",
    var token: String? = null,
    var eventFilePath: String? = null,
)

open class BaselineConfiguration(
    var searchStrategy: BaselineSearchStrategy? = null,
    var tagPattern: String? = null,
    var targetRef: String? = null,
)

open class RecommendedTestsConfiguration(
    var enabled: Boolean = true,
    var coveragePeriodDays: Int? = null,
)

abstract class AgentMavenConfiguration(
    var version: String? = null,
    var downloadUrl: String? = null,
    var zipPath: String? = null,

    var logLevel: String? = "INFO",
    var logFile: String? = null,

    var additionalParams: Map<String, String>? = null,
)

open class TestAgentMavenConfiguration(
    var testTaskId: String? = null,
    var testTracingEnabled: Boolean? = null,
    var testLaunchMetadataSendingEnabled: Boolean? = null,
) : AgentMavenConfiguration()

open class AppAgentMavenConfiguration(
    var envId: String? = null,
    var agentMode: String? = null
) : AgentMavenConfiguration()

open class AppArchiveScannerMavenConfiguration(
) : AgentMavenConfiguration()