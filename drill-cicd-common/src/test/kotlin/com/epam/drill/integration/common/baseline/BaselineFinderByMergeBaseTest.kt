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

import com.epam.drill.integration.common.GitTestBase
import com.epam.drill.integration.common.git.impl.GitClientImpl
import kotlin.test.*

class BaselineFinderByMergeBaseTest: GitTestBase() {
    private lateinit var finder: BaselineFinderByMergeBase

    @BeforeTest
    fun init() {
        finder = BaselineFinderByMergeBase(GitClientImpl(workingDir))
    }

    @Test
    fun `if there were no merges, findBaseline should return first commit before branching`() {
        exec("git init -b main")
        exec("git commit --allow-empty -m \"Initial commit in the main branch\"")
        val commitInMainBeforeBranching = exec("git rev-parse HEAD")
        exec("git checkout -b test-branch")
        exec("git commit --allow-empty -m \"Add the first commit in the test branch\"")
        exec("git checkout main")
        exec("git commit --allow-empty -m \"Add the second commit in the main branch\"")
        exec("git checkout test-branch")
        exec("git commit --allow-empty -m \"Add the second commit in the test branch\"")

        val baselineCommit = finder.findBaseline(MergeBaseCriteria("main"))

        assertEquals(commitInMainBeforeBranching, baselineCommit)
    }

    @Test
    fun `if there was a merge, findBaseline should return commit that was merged`() {
        exec("git init -b main")
        exec("git commit --allow-empty -m \"Initial commit in the main branch\"")
        exec("git checkout -b test-branch")
        exec("git commit --allow-empty -m \"Add the first commit in the test branch\"")
        exec("git checkout main")
        exec("git commit --allow-empty -m \"Add the second commit in the main branch\"")
        val commitInMainBeforeMerging = exec("git rev-parse HEAD")
        exec("git checkout test-branch")
        exec("git commit --allow-empty -m \"Add the second commit in the test branch\"")
        exec("git merge main")

        val baselineCommit = finder.findBaseline(MergeBaseCriteria("main"))

        assertEquals(commitInMainBeforeMerging, baselineCommit)
    }

}