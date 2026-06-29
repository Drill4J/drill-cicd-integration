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

import com.epam.drill.integration.common.client.BuildView
import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.client.TestView
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    override suspend fun findBuild(
        groupId: String,
        appId: String,
        commitSha: String?,
        buildVersion: String?,
        sortBy: String?,
        sortOrder: String?
    ): BuildView? {
        val url = "$metricsUrl/builds"
        val response = client.request<JsonObject>(url) {
            parameter("groupId", groupId)
            parameter("appId", appId)
            commitSha?.let { parameter("commitSha", it) }
            buildVersion?.let { parameter("buildVersion", it) }
            parameter("sortBy", "COMMIT_DATE")
            parameter("sortOrder", "DESC")
            parameter("pageSize", 1)

            contentType(ContentType.Application.Json)
            apiKey?.let { apiKey ->
                headers {
                    append(API_KEY_HEADER, apiKey)
                }
            }
        }.getValue("data").jsonArray.firstOrNull()?.jsonObject?.let { buildJson ->
            BuildView(
                id = buildJson.getValue("id").jsonPrimitive.content,
                groupId = buildJson.getValue("groupId").jsonPrimitive.content,
                appId = buildJson.getValue("appId").jsonPrimitive.content,
                commitSha = buildJson["commitSha"]?.jsonPrimitive?.content,
                buildVersion = buildJson["buildVersion"]?.jsonPrimitive?.content,
            )
        }
        return response
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

    override suspend fun getImpactedTests(
        groupId: String,
        appId: String,
        commitSha: String?,
        buildVersion: String?,
        baselineCommitSha: String?,
        baselineBuildVersion: String?,
        testsToSkip: Boolean,
        limit: Int?
    ): List<TestView> {
        val url = "$metricsUrl/impacted-tests"
        val response = client.request<JsonObject>(url) {
            parameter("groupId", groupId)
            parameter("appId", appId)
            commitSha?.let { parameter("commitSha", it) }
            buildVersion?.let { parameter("buildVersion", it) }
            baselineCommitSha?.let { parameter("baselineCommitSha", it) }
            baselineBuildVersion?.let { parameter("baselineBuildVersion", it) }
            takeIf { testsToSkip }?.let { parameter("impactStatuses", "NOT_IMPACTED") }
            limit?.let { parameter("pageSize", it) }

            contentType(ContentType.Application.Json)
            apiKey?.let { apiKey ->
                headers {
                    append(API_KEY_HEADER, apiKey)
                }
            }
        }.getValue("data").jsonArray.map { it.jsonObject }.map { testJson ->
            TestView(
                testDefinitionId = testJson.getValue("testDefinitionId").jsonPrimitive.content,
                testRunner = testJson["testRunner"]?.jsonPrimitive?.content,
                testPath = testJson.getValue("testPath").jsonPrimitive.content,
                testName = testJson.getValue("testName").jsonPrimitive.content,
            )
        }
        return response
    }
}

