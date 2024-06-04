package com.epam.drill.integration.common.client

import kotlinx.serialization.Serializable

interface DataIngestClient {
    suspend fun sendBuild(payload: BuildPayload)
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