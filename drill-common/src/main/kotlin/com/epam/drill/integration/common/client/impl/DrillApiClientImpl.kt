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
package com.epam.drill.integration.common.client.impl

import com.epam.drill.integration.common.client.BuildPayload
import com.epam.drill.integration.common.client.DrillApiClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject

private const val API_KEY_HEADER = "-X-Api-Key"

class DrillApiClientImpl(
    private val drillUrl: String,
    private val drillApiKey: String? = null,
): DrillApiClient {
    private val client = HttpClient(CIO) {
        install(JsonFeature)
    }

    override suspend fun getDiffMetricsByCommits(
        groupId: String,
        agentId: String,
        sourceCommitSha: String,
        baseCommitSha: String
    ): JsonObject {
        return getMetricsSummary(
            groupId = groupId,
            agentId = agentId,
            sourceCommitSha = sourceCommitSha,
            baseCommitSha = baseCommitSha)
    }

    override suspend fun getDiffMetricsByBranches(
        groupId: String,
        agentId: String,
        sourceBranch: String,
        targetBranch: String,
        latestCommitSha: String
    ): JsonObject {
        return getMetricsSummary(
            groupId = groupId,
            agentId = agentId,
            sourceBranch = sourceBranch,
            targetBranch = targetBranch,
            sourceCommitSha = latestCommitSha)
    }

    override suspend fun postBuild(payload: BuildPayload) {
        val url = "$drillUrl/api/builds"
        client.post<JsonObject?>(url) {
            contentType(ContentType.Application.Json)
            drillApiKey?.let { apiKey ->
                headers {
                    append(API_KEY_HEADER, apiKey)
                }
            }
            body = payload
        }
        TODO("Not yet implemented")
    }

    private suspend fun getMetricsSummary(
        groupId: String,
        agentId: String,
        sourceCommitSha: String? = "",
        sourceBranch: String? = "",
        baseCommitSha: String? = "",
        targetBranch: String? = ""
    ): JsonObject {

        val url = "$drillUrl/api/metrics/summary"
        val response = client.request<JsonObject>(url) {
            parameter("groupId", groupId)
            parameter("agentId", agentId)
            parameter("currentVcsRef", sourceCommitSha)
            parameter("currentBranch", sourceBranch)
            parameter("baseVcsRef", baseCommitSha)
            parameter("baseBranch", targetBranch)

            contentType(ContentType.Application.Json)
            drillApiKey?.let { apiKey ->
                headers {
                    append(API_KEY_HEADER, apiKey)
                }
            }
        }
        return response
    }
}

