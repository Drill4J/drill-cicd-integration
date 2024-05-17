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
package com.epam.drill.integration.gitlab.client.impl

import com.epam.drill.integration.gitlab.client.GitlabApiClient
import kotlinx.serialization.json.JsonObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*

class GitlabApiClientV4Impl(
    private val gitlabApiUrl: String,
    private val gitlabPrivateToken: String? = null
) : GitlabApiClient {
    private val client = HttpClient(CIO) {
        install(JsonFeature)
    }

    override suspend fun postMergeRequestReport(projectId: String, mergeRequestId: String, comment: String) {
        val url = "$gitlabApiUrl/v4/projects/$projectId/merge_requests/$mergeRequestId/notes"
        client.post<JsonObject>(url) {
            contentType(ContentType.Application.Json)
            gitlabPrivateToken?.let { token ->
                headers {
                    append("Private-Token", token)
                }
            }
            body = mapOf("body" to comment)
        }
    }
}