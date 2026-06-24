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
package com.epam.drill.integration.common.baseline

import com.epam.drill.integration.common.client.MetricsClient
import mu.KotlinLogging

class BaselineFinderByBuildVersion(
    private val metricsClient: MetricsClient,
) : BaselineFinder<BuildVersionCriteria> {
    private val logger = KotlinLogging.logger {}

    override suspend fun findBaseline(groupId: String, appId: String, criteria: BuildVersionCriteria): Baseline {
        logger.info { "Looking for build with buildVersion=${criteria.buildVersion}..." }
        val build = metricsClient.findBuild(groupId = groupId, appId = appId, buildVersion = criteria.buildVersion)
        return build?.let {
            Baseline(
                buildVersion = it.buildVersion,
                commitSha = it.commitSha,
            )
        } ?: throw IllegalStateException("No build found for buildVersion ${criteria.buildVersion}")
    }
}

class BuildVersionCriteria(val buildVersion: String) : BaselineSearchCriteria

