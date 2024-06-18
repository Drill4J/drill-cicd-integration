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

import com.epam.drill.integration.common.git.GitClient
import mu.KotlinLogging

class BaselineFinderByMergeBase(
    private val gitClient: GitClient
) : BaselineFinder<MergeBaseCriteria> {
    private val logger = KotlinLogging.logger {}

    override fun findBaseline(criteria: MergeBaseCriteria): String {
        logger.info { "Looking for merge base for ${criteria.targetRef}..." }
        return gitClient.getMergeBaseCommitSha(criteria.targetRef)
    }
}

class MergeBaseCriteria(val targetRef: String): BaselineSearchCriteria