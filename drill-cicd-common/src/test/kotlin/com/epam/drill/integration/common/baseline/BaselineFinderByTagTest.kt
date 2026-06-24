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

class BaselineFinderByTagTest : GitTestBase() {
    private lateinit var metricsClient: MetricsClient
    private lateinit var finder: BaselineFinderByTag

    @BeforeTest
    fun init() {
        metricsClient = mock()
        finder = BaselineFinderByTag(GitClientImpl(workingDir), metricsClient)
    }

    @Test
    fun `given existing git tag, findBaseline should return commit sha`(): Unit = runBlocking {
        exec("git init")
        exec("git commit --allow-empty -m \"Initial commit\"")
        exec("git commit --allow-empty -m \"Add file2.txt\"")
        exec("git tag -a v1.0 -m \"Version 1.0\"")
        val tagCommitSha = exec("git rev-parse HEAD")
        exec("git commit --allow-empty -m \"Update file1.txt\"")

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "group", "app", tagCommitSha, "1.0"))

        val baseline = finder.findBaseline("group", "app", TagCriteria("v[0-9].[0.9]"))

        assertNotNull(baseline.commitSha)
    }

    @Test
    fun `given existing git tag and matchBy BUILD_VERSION, findBaseline should search by buildVersion extracted from tag`(): Unit = runBlocking {
        exec("git init")
        exec("git commit --allow-empty -m \"Initial commit\"")
        exec("git tag -a v1.2.3 -m \"Version 1.2.3\"")
        exec("git commit --allow-empty -m \"Next commit\"")

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), eq("1.2.3"), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "group", "app", null, "1.2.3"))

        val baseline = finder.findBaseline(
            "group", "app",
            TagCriteria(tagPattern = "v*", matchBy = TagMatchBy.BUILD_VERSION, tagPrefix = "v")
        )

        verify(metricsClient).findBuild(eq("group"), eq("app"), isNull(), eq("1.2.3"), isNull(), isNull())
        assertEquals("1.2.3", baseline.buildVersion)
    }

    @Test
    fun `if there are no git tags, findBaseline should throw error`(): Unit = runBlocking {
        exec("git init")
        exec("git commit --allow-empty -m \"Initial commit: add file1.txt\"")
        exec("git commit --allow-empty -m \"Add file2.txt\"")
        exec("git commit --allow-empty -m \"Update file1.txt\"")

        assertFailsWith<IllegalStateException> {
            finder.findBaseline("group", "app", TagCriteria("v[0-9].[0.9]"))
        }
    }

}