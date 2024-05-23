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
    @Parameter(property = "drillApiUrl")
    var drillApiUrl: String? = null,
    @Parameter(property = "drillApiKey")
    var drillApiKey: String? = null,
    @Parameter(property = "groupId")
    var groupId: String? = null,
    @Parameter(property = "agentId")
    var agentId: String? = null
)

open class DrillCiCdProperties(
    @Parameter(property = "latestCommitSha")
    var latestCommitSha: String? = null,
    @Parameter(property = "sourceBranch")
    var sourceBranch: String? = null,
    @Parameter(property = "targetBranch")
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
    @Parameter(property = "gitlabApiUrl")
    var gitlabApiUrl: String? = null,
    @Parameter(property = "gitlabPrivateToken")
    var gitlabPrivateToken: String? = null,
    @Parameter(property = "projectId")
    var projectId: String? = null,
    @Parameter(property = "mergeRequestId")
    var mergeRequestId: String? = null
)

open class DrillGithubProperties(
    @Parameter(property = "githubApiUrl")
    var githubApiUrl: String = "https://api.github.com",
    @Parameter(property = "githubToken")
    var githubToken: String? = null,
    @Parameter(property = "githubRepository")
    var githubRepository: String? = null,
    @Parameter(property = "pullRequestId")
    var pullRequestId: Int? = null
)