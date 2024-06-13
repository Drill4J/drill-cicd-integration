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
import com.epam.drill.integration.common.client.DataIngestClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*

private const val API_KEY_HEADER = "X-Api-Key"

class DataIngestClientImpl(
    private val drillApiUrl: String,
    private val drillApiKey: String? = null,
) : DataIngestClient {
    private val dataIngestUrl = "${drillApiUrl.removeSuffix("/")}/data-ingest"

    private val client = HttpClient(CIO) {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    override suspend fun sendBuild(payload: BuildPayload) {
        val url = "$dataIngestUrl/builds"
        client.put<Any?>(url) {
            contentType(ContentType.Application.Json)
            drillApiKey?.let { apiKey ->
                headers {
                    append(API_KEY_HEADER, apiKey)
                }
            }
            body = payload
        }
    }

}