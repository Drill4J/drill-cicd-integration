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

import com.epam.drill.integration.common.client.MetricsClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import mu.KotlinLogging

private const val API_KEY_HEADER = "X-Api-Key"

class MetricsClientImpl(
    private val apiUrl: String,
    private val apiKey: String? = null,
) : MetricsClient {
    private val metricsUrl = "${apiUrl.removeSuffix("/")}/metrics"

    private val client = HttpClient(CIO) {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    override suspend fun getBuildComparison(
        groupId: String,
        appId: String,
        instanceId: String?,
        commitSha: String?,
        buildVersion: String?,
        baselineInstanceId: String?,
        baselineCommitSha: String?,
        baselineBuildVersion: String?,
        coverageThreshold: Double?,
    ): JsonObject {

        val url = "$metricsUrl/build-diff-report"
        val response = client.request<JsonObject>(url) {
            parameter("groupId", groupId)
            parameter("appId", appId)
            parameter("instanceId", instanceId)
            parameter("commitSha", commitSha)
            parameter("buildVersion", buildVersion)
            parameter("baselineInstanceId", baselineInstanceId)
            parameter("baselineCommitSha", baselineCommitSha)
            parameter("baselineBuildVersion", baselineBuildVersion)
            parameter("coverageThreshold", coverageThreshold)

            contentType(ContentType.Application.Json)
            apiKey?.let { apiKey ->
                headers {
                    append(API_KEY_HEADER, apiKey)
                }
            }
        }
        return response
    }
}

