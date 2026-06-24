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
import com.epam.drill.integration.common.client.BuildView
import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.git.impl.GitClientImpl
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.*
import kotlin.test.*

class BaselineFinderByMergeBaseTest : GitTestBase() {
    private lateinit var metricsClient: MetricsClient
    private lateinit var finder: BaselineFinderByMergeBase

    @BeforeTest
    fun init() {
        metricsClient = mock()
        finder = BaselineFinderByMergeBase(GitClientImpl(workingDir), metricsClient)
    }

    @Test
    fun `if there were no merges, findBaseline should return first commit before branching`(): Unit = runBlocking {
        exec("git init -b main")
        exec("git commit --allow-empty -m \"Initial commit in the main branch\"")
        val commitInMainBeforeBranching = exec("git rev-parse HEAD")
        exec("git checkout -b test-branch")
        exec("git commit --allow-empty -m \"Add the first commit in the test branch\"")
        exec("git checkout main")
        exec("git commit --allow-empty -m \"Add the second commit in the main branch\"")
        exec("git checkout test-branch")
        exec("git commit --allow-empty -m \"Add the second commit in the test branch\"")

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "group", "app", commitInMainBeforeBranching, "1.0"))

        val baseline = finder.findBaseline("group", "app", MergeBaseCriteria("main"))

        verify(metricsClient).findBuild(eq("group"), eq("app"), eq(commitInMainBeforeBranching), isNull(), isNull(), isNull())
        assertEquals(commitInMainBeforeBranching, baseline.commitSha)
    }

    @Test
    fun `if there was a merge, findBaseline should return commit that was merged`(): Unit = runBlocking {
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

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "group", "app", commitInMainBeforeMerging, "1.0"))

        val baseline = finder.findBaseline("group", "app", MergeBaseCriteria("main"))

        verify(metricsClient).findBuild(eq("group"), eq("app"), eq(commitInMainBeforeMerging), isNull(), isNull(), isNull())
        assertEquals(commitInMainBeforeMerging, baseline.commitSha)
    }
}