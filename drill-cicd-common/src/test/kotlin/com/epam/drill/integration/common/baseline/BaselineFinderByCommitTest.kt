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

import com.epam.drill.integration.common.client.BuildView
import com.epam.drill.integration.common.client.MetricsClient
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.*
import kotlin.test.*

class BaselineFinderByCommitTest {
    private lateinit var metricsClient: MetricsClient
    private lateinit var finder: BaselineFinderByCommit

    @BeforeTest
    fun init() {
        metricsClient = mock()
        finder = BaselineFinderByCommit(metricsClient)
    }

    @Test
    fun `given a commitSha, findBaseline should return the matching build`(): Unit = runBlocking {
        val commitSha = "a1b2c3d4e5f6"
        whenever(metricsClient.findBuild(any(), any(), eq(commitSha), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "group", "app", commitSha, "1.0.0"))

        val baseline = finder.findBaseline("group", "app", CommitCriteria(commitSha))

        verify(metricsClient).findBuild(eq("group"), eq("app"), eq(commitSha), isNull(), isNull(), isNull())
        assertEquals(commitSha, baseline.commitSha)
        assertEquals("1.0.0", baseline.buildVersion)
    }

    @Test
    fun `if no build is found for the commitSha, findBaseline should throw error`(): Unit = runBlocking {
        val commitSha = "deadbeef"
        whenever(metricsClient.findBuild(any(), any(), eq(commitSha), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(null)

        assertFailsWith<IllegalStateException> {
            finder.findBaseline("group", "app", CommitCriteria(commitSha))
        }
    }
}

