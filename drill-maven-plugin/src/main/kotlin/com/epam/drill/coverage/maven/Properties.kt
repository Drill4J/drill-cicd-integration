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
package com.epam.drill.coverage.maven

import org.apache.maven.plugins.annotations.Parameter

open class DrillProperties(
    @Parameter(property = "drillApiUrl", required = true)
    var drillApiUrl: String? = null,
    @Parameter(property = "drillApiKey")
    var drillApiKey: String? = null,
    @Parameter(property = "groupId", required = true)
    var groupId: String? = null,
    @Parameter(property = "agentId", required = true)
    var agentId: String? = null
)

open class DrillCiCdProperties(
    @Parameter(property = "latestCommitSha", required = true)
    var latestCommitSha: String? = null,
    @Parameter(property = "sourceBranch", required = true)
    var sourceBranch: String? = null,
    @Parameter(property = "targetBranch", required = true)
    var targetBranch: String? = null,
    var gitlab: DrillGitlabProperties? = null,
    var github: DrillGithubProperties? = null
) : DrillProperties() {
    fun gitlab(configure: DrillGitlabProperties.() -> Unit) {
        this.gitlab = DrillGitlabProperties().apply(configure)
    }

    fun github(configure: DrillGithubProperties.() -> Unit) {
        this.github = DrillGithubProperties().apply(configure)
    }
}

open class DrillGitlabProperties(
    @Parameter(property = "gitlabApiUrl", required = true)
    var gitlabApiUrl: String? = null,
    @Parameter(property = "gitlabPrivateToken", required = true)
    var gitlabPrivateToken: String? = null,
    @Parameter(property = "projectId", required = true)
    var projectId: String? = null,
    @Parameter(property = "mergeRequestId", required = true)
    var mergeRequestId: String? = null
)

open class DrillGithubProperties(
    @Parameter(property = "githubApiUrl")
    var githubApiUrl: String = "https://api.github.com",
    @Parameter(property = "githubToken", required = true)
    var githubToken: String? = null,
    @Parameter(property = "githubRepository", required = true)
    var githubRepository: String? = null,
    @Parameter(property = "pullRequestId", required = true)
    var pullRequestId: Int? = null
)
