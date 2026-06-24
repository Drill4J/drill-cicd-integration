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
import com.epam.drill.integration.common.git.GitClient
import mu.KotlinLogging

class BaselineFinderByMergeBase(
    private val gitClient: GitClient,
    private val metricsClient: MetricsClient,
) : BaselineFinder<MergeBaseCriteria> {
    private val logger = KotlinLogging.logger {}

    override suspend fun findBaseline(groupId: String, appId: String, criteria: MergeBaseCriteria): Baseline {
        logger.info { "Looking for merge base for ${criteria.targetRef}..." }
        val mergeBaseCommitSha = gitClient.getMergeBaseCommitSha(criteria.targetRef)
        val build = metricsClient.findBuild(groupId = groupId, appId = appId, commitSha = mergeBaseCommitSha)
        return build?.let {
            Baseline(
                buildVersion = it.buildVersion,
                commitSha = it.commitSha,
            )
        } ?: throw IllegalStateException("No build found for merge base commit $mergeBaseCommitSha")
    }
}

class MergeBaseCriteria(val targetRef: String): BaselineSearchCriteria