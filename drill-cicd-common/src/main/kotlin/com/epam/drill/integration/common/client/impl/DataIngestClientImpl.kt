package com.epam.drill.integration.common.client.impl

import com.epam.drill.integration.common.client.BuildPayload
import com.epam.drill.integration.common.client.DataIngestClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject

private const val API_KEY_HEADER = "X-Api-Key"

class DataIngestClientImpl(
    private val drillApiUrl: String,
    private val drillApiKey: String? = null,
) : DataIngestClient {
    private val dataIngestUrl = "${drillApiUrl.removeSuffix("/")}/data-ingest"

    private val client = HttpClient(CIO) {
        install(JsonFeature)
    }

    override suspend fun sendBuild(payload: BuildPayload) {
        val url = "$dataIngestUrl/builds"
        client.put<JsonObject?>(url) {
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