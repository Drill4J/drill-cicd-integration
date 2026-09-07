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
import com.epam.drill.integration.common.git.GIT_INVALID_ARGUMENT_ERROR
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.GitException
import mu.KotlinLogging

class BaselineFinderByTag(
    private val gitClient: GitClient,
    private val metricsClient: MetricsClient,
) : BaselineFinder<TagCriteria> {
    private val logger = KotlinLogging.logger {}

    override suspend fun findBaseline(groupId: String, appId: String, criteria: TagCriteria): Baseline {
        logger.info { "Looking for git tag ${criteria.tagPattern}..." }
        val tag = try {
            gitClient.describe(tags = true, matchPattern = criteria.tagPattern)
        } catch (e: GitException) {
            if (e.exitCode == GIT_INVALID_ARGUMENT_ERROR)
                throw IllegalStateException("No git tags found matching pattern ${criteria.tagPattern}", e)
            else
                throw e
        }
        return when (criteria.matchBy) {
            TagMatchBy.BUILD_VERSION -> {
                val buildVersion = tag.removePrefix(criteria.tagPrefix)
                logger.info { "Looking for build with buildVersion=$buildVersion (from tag $tag)..." }
                val build = metricsClient.findBuild(groupId = groupId, appId = appId, buildVersion = buildVersion)
                build?.let {
                    Baseline(
                        buildVersion = it.buildVersion,
                        commitSha = it.commitSha,
                    )
                } ?: throw IllegalStateException("No build found for git tag $tag with buildVersion $buildVersion")
            }
            TagMatchBy.COMMIT_SHA -> {
                val tagCommitSha = gitClient.revList(ref = tag).first()
                logger.info { "Looking for build with commitSha=$tagCommitSha (from tag $tag)..." }
                val build = metricsClient.findBuild(groupId = groupId, appId = appId, commitSha = tagCommitSha)
                build?.let {
                    Baseline(
                        buildVersion = it.buildVersion,
                        commitSha = it.commitSha,
                    )
                } ?: throw IllegalStateException("No build found for git tag ${criteria.tagPattern} with commit sha $tagCommitSha")
            }
        }
    }
}

enum class TagMatchBy {
    COMMIT_SHA,
    BUILD_VERSION
}

class TagCriteria(
    val tagPattern: String,
    val matchBy: TagMatchBy = TagMatchBy.COMMIT_SHA,
    val tagPrefix: String = "",
) : BaselineSearchCriteria
