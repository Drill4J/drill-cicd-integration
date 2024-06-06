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
    var appId: String? = null,
    var buildVersion: String? = null,
)

open class DrillCiCdProperties(
    var gitlab: DrillGitlabProperties? = null,
    var github: DrillGithubProperties? = null,
) : DrillProperties() {
    fun gitlab(configure: DrillGitlabProperties.() -> Unit) {
        this.gitlab = DrillGitlabProperties().apply(configure)
    }

    fun github(configure: DrillGithubProperties.() -> Unit) {
        this.github = DrillGithubProperties().apply(configure)
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