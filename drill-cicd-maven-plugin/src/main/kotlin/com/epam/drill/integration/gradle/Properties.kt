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
import org.apache.maven.plugins.annotations.Parameter


open class DrillGitlabProperties(
    @Parameter(property = "apiUrl", required = true)
    var apiUrl: String? = null,
    @Parameter(property = "privateToken", required = true)
    var privateToken: String? = null,
    @Parameter(property = "projectId")
    var projectId: String? = null,
    @Parameter(property = "commitSha")
    var commitSha: String? = null,
    @Parameter(property = "mergeRequest")
    var mergeRequest: MergeRequest = MergeRequest()
)

open class MergeRequest(
    @Parameter(property = "mergeRequestIid")
    var mergeRequestIid: String? = null,
    @Parameter(property = "mergeBaseCommitSha")
    var mergeBaseCommitSha: String? = null,
    @Parameter(property = "sourceBranch")
    var sourceBranch: String? = null,
    @Parameter(property = "targetBranch")
    var targetBranch: String? = null,
)

open class DrillGithubProperties(
    @Parameter(property = "apiUrl")
    var apiUrl: String = "https://api.github.com",
    @Parameter(property = "token", required = true)
    var token: String? = null,
    @Parameter(property = "eventFilePath")
    var eventFilePath: String? = null,
)

open class DrillBaselineProperties(
    @Parameter(property = "searchStrategy")
    var searchStrategy: BaselineSearchStrategy? = null,
    @Parameter(property = "tagPattern")
    var tagPattern: String? = null,
    @Parameter(property = "targetRef")
    var targetRef: String? = null,
)
