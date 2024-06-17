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

import com.epam.drill.integration.common.git.GIT_INVALID_ARGUMENT_ERROR
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.GitException
import mu.KotlinLogging

class BaselineFinderByTag(
    private val gitClient: GitClient
) : BaselineFinder<TagCriteria> {
    private val logger = KotlinLogging.logger {}

    override fun findBaseline(criteria: TagCriteria): String = try {
        logger.info { "Looking for git tag ${criteria.tagPattern}..." }
        val tag = gitClient.describe(tags = true, matchPattern = criteria.tagPattern)
        gitClient.revList(ref = tag).first()
    } catch (e: GitException) {
        if (e.exitCode == GIT_INVALID_ARGUMENT_ERROR)
            throw IllegalStateException("No git tags found matching pattern ${criteria.tagPattern}", e)
        else
            throw e
    }
}

class TagCriteria(val tagPattern: String) : BaselineSearchCriteria