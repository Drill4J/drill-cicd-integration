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
package com.epam.drill.integration.common

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject

object DrillApiClient {
    private val client = HttpClient(CIO) {
        install(JsonFeature)
    }

    suspend fun getMetricsSummary(payload: DrillCIIntegration): JsonObject {

        val url = "${payload.drillUrl}/api/metrics/summary"
        val response = client.request<JsonObject>(url) {
            parameter("groupId", payload.groupId)
            parameter("agentId", payload.agentId)
            parameter("currentVcsRef", payload.latestCommitSha)
            parameter("currentBranch", payload.sourceBranch)
            parameter("baseVcsRef", payload.previousLatestCommitSha)
            parameter("baseBranch", payload.targetBranch)

            contentType(ContentType.Application.Json)
            payload.drillApiKey?.let { apiKey ->
                headers {
                    append("-X-Api-Key", apiKey)
                }
            }
        }
        return response
    }
}

