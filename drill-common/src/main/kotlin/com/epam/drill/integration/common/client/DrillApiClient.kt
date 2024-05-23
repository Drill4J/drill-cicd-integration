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
package com.epam.drill.integration.common.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

interface DrillApiClient {
    suspend fun getDiffMetricsByCommits(
        groupId: String,
        appId: String,
        sourceCommitSha: String,
        baseCommitSha: String
    ): JsonObject

    suspend fun getDiffMetricsByBranches(
        groupId: String,
        appId: String,
        sourceBranch: String,
        targetBranch: String,
        commitSha: String
    ): JsonObject

    suspend fun postBuild(payload: BuildPayload)
}

@Serializable
class BuildPayload(
    val groupId: String,
    val appId: String,
    val commitSha: String,
    val buildVersion: String? = null,
    val branch: String? = null,
    val commitDate: String,
    val commitMessage: String,
    val commitAuthor: String
)