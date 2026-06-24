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

class BaselineFinderByBuildVersionTest {
    private lateinit var metricsClient: MetricsClient
    private lateinit var finder: BaselineFinderByBuildVersion

    @BeforeTest
    fun init() {
        metricsClient = mock()
        finder = BaselineFinderByBuildVersion(metricsClient)
    }

    @Test
    fun `given a buildVersion, findBaseline should return the matching build`(): Unit = runBlocking {
        val buildVersion = "0.1.1"
        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), eq(buildVersion), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "group", "app", "abc123", buildVersion))

        val baseline = finder.findBaseline("group", "app", BuildVersionCriteria(buildVersion))

        verify(metricsClient).findBuild(eq("group"), eq("app"), isNull(), eq(buildVersion), isNull(), isNull())
        assertEquals(buildVersion, baseline.buildVersion)
        assertEquals("abc123", baseline.commitSha)
    }

    @Test
    fun `if no build is found for the buildVersion, findBaseline should throw error`(): Unit = runBlocking {
        val buildVersion = "9.9.9"
        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), eq(buildVersion), anyOrNull(), anyOrNull()))
            .thenReturn(null)

        assertFailsWith<IllegalStateException> {
            finder.findBaseline("group", "app", BuildVersionCriteria(buildVersion))
        }
    }
}

