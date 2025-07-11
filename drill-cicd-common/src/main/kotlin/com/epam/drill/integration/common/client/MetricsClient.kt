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

import kotlinx.serialization.json.JsonObject

interface MetricsClient {

    suspend fun getBuildComparison(
        groupId: String,
        appId: String,
        instanceId: String? = null,
        commitSha: String? = null,
        buildVersion: String? = null,
        baselineInstanceId: String? = null,
        baselineCommitSha: String? = null,
        baselineBuildVersion: String? = null,
        coverageThreshold: Double? = null,
        useMaterializedViews: Boolean? = null,
    ): JsonObject
}