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

class BaselineFinderByTagTest: GitTestBase() {
    private lateinit var finder: BaselineFinderByTag

    @BeforeTest
    fun init() {
        finder = BaselineFinderByTag(GitClientImpl(workingDir))
    }

    @Test
    fun `given existing git tag, findBaseline should return commit sha`() {
        exec("git init")
        exec("git commit --allow-empty -m \"Initial commit\"")
        exec("git commit --allow-empty -m \"Add file2.txt\"")
        exec("git tag -a v1.0 -m \"Version 1.0\"")
        exec("git commit --allow-empty -m \"Update file1.txt\"")

        val sha = finder.findBaseline(TagCriteria("v[0-9].[0.9]"))

        assertNotNull(sha)
    }

    @Test
    fun `if there are no git tags, findBaseline should throw error`() {
        exec("git init")
        exec("git commit --allow-empty -m \"Initial commit: add file1.txt\"")
        exec("git commit --allow-empty -m \"Add file2.txt\"")
        exec("git commit --allow-empty -m \"Update file1.txt\"")

        assertFailsWith<IllegalStateException> {
            finder.findBaseline(TagCriteria("v[0-9].[0.9]"))
        }
    }

}