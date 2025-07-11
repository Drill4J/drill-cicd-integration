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

class BaselineFactory(
    private val gitClient: GitClient
) {
    private val baselineFinders: (BaselineSearchStrategy) -> BaselineFinder<BaselineSearchCriteria> = { strategy ->
        @Suppress("UNCHECKED_CAST")
        when (strategy) {
            BaselineSearchStrategy.SEARCH_BY_TAG -> BaselineFinderByTag(gitClient)
            BaselineSearchStrategy.SEARCH_BY_MERGE_BASE -> BaselineFinderByMergeBase(gitClient)
        } as BaselineFinder<BaselineSearchCriteria>
    }

    fun produce(strategy: BaselineSearchStrategy): BaselineFinder<BaselineSearchCriteria> = baselineFinders(strategy)
}