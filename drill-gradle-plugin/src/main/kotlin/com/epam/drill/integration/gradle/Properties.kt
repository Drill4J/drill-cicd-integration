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


open class DrillProperties(
    var drillApiUrl: String? = null,
    var drillApiKey: String? = null,
    var groupId: String? = null,
    var agentId: String? = null,
)

open class DrillCiCdProperties(
    var latestCommitSha: String? = null,
    var sourceBranch: String? = null,
    var targetBranch: String? = null,
    var gitlab: DrillGitlabProperties,
    var github: DrillGithubProperties,
): DrillProperties()

class DrillGitlabProperties(
    var gitlabApiUrl: String? = null,
    var gitlabPrivateToken: String? = null,
    var projectId: String? = null,
    var mergeRequestId: String? = null,
)

class DrillGithubProperties(
    var githubApiUrl: String = "https://api.github.com",
    var githubToken: String? = null,
    var githubRepository: String? = null,
    var pullRequestId: Int? = null
)