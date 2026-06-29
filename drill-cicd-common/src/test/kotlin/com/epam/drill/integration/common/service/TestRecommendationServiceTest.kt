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
package com.epam.drill.integration.common.service

import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.BuildVersionCriteria
import com.epam.drill.integration.common.client.MetricsClient
import com.epam.drill.integration.common.client.TestView
import com.epam.drill.integration.common.client.BuildView
import com.epam.drill.integration.common.git.GitClient
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.*
import java.nio.file.Files
import kotlin.test.*

class TestRecommendationServiceTest {

    private lateinit var metricsClient: MetricsClient
    private lateinit var gitClient: GitClient
    private lateinit var service: TestRecommendationService
    private lateinit var outputDir1: String
    private lateinit var outputDir2: String

    @BeforeTest
    fun setUp() {
        metricsClient = mock()
        gitClient = mock()
        service = TestRecommendationService(
            metricsClient = metricsClient,
            gitClient = gitClient,
        )
        outputDir1 = Files.createTempDirectory("drill-module1-").toFile().absolutePath
        outputDir2 = Files.createTempDirectory("drill-module2-").toFile().absolutePath

        // Clear the shared cache before each test to avoid cross-test pollution
        TestRecommendationService.impactedTestsCache.clear()
    }

    @AfterTest
    fun tearDown() {
        TestRecommendationService.impactedTestsCache.clear()
    }

    @Test
    fun `given same parameters called twice, getImpactedTests should only be called once`(): Unit = runBlocking {
        val tests = listOf(
            TestView(testDefinitionId = "id1", testPath = "com/example/FooTest", testName = "testFoo"),
            TestView(testDefinitionId = "id2", testPath = "com/example/BarTest", testName = "testBar"),
        )
        whenever(
            metricsClient.getImpactedTests(
                groupId = any(), appId = any(),
                commitSha = anyOrNull(), buildVersion = anyOrNull(),
                baselineCommitSha = anyOrNull(), baselineBuildVersion = anyOrNull(),
                testsToSkip = any(), limit = anyOrNull()
            )
        ).thenReturn(tests)

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "myGroup", "myApp", null, "1.0.0"))

        val criteria = BuildVersionCriteria("1.0.0")

        // First call — simulates module1
        service.getRecommendedTests(
            groupId = "myGroup",
            appId = "myApp",
            buildVersion = "2.0.0",
            baselineSearchStrategy = BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION,
            baselineSearchCriteria = criteria,
            outputPath = outputDir1,
        )

        // Second call with identical parameters — simulates module2
        service.getRecommendedTests(
            groupId = "myGroup",
            appId = "myApp",
            buildVersion = "2.0.0",
            baselineSearchStrategy = BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION,
            baselineSearchCriteria = criteria,
            outputPath = outputDir2,
        )

        // The HTTP call must happen exactly once
        verify(metricsClient, times(1)).getImpactedTests(
            groupId = eq("myGroup"),
            appId = eq("myApp"),
            commitSha = isNull(),
            buildVersion = eq("2.0.0"),
            baselineCommitSha = anyOrNull(),
            baselineBuildVersion = anyOrNull(),
            testsToSkip = eq(true),
            limit = any(),
        )
    }

    @Test
    fun `given different buildVersions, getImpactedTests should be called for each`(): Unit = runBlocking {
        val tests = listOf(
            TestView(testDefinitionId = "id1", testPath = "com/example/FooTest", testName = "testFoo"),
        )
        whenever(
            metricsClient.getImpactedTests(
                groupId = any(), appId = any(),
                commitSha = anyOrNull(), buildVersion = anyOrNull(),
                baselineCommitSha = anyOrNull(), baselineBuildVersion = anyOrNull(),
                testsToSkip = any(), limit = anyOrNull()
            )
        ).thenReturn(tests)

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "myGroup", "myApp", null, "1.0.0"))

        service.getRecommendedTests(
            groupId = "myGroup",
            appId = "myApp",
            buildVersion = "2.0.0",
            baselineSearchStrategy = BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION,
            baselineSearchCriteria = BuildVersionCriteria("1.0.0"),
            outputPath = outputDir1,
        )

        service.getRecommendedTests(
            groupId = "myGroup",
            appId = "myApp",
            buildVersion = "3.0.0", // different buildVersion
            baselineSearchStrategy = BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION,
            baselineSearchCriteria = BuildVersionCriteria("1.0.0"),
            outputPath = outputDir2,
        )

        // Each unique combination must trigger its own HTTP call
        verify(metricsClient, times(2)).getImpactedTests(
            groupId = any(), appId = any(),
            commitSha = anyOrNull(), buildVersion = anyOrNull(),
            baselineCommitSha = anyOrNull(), baselineBuildVersion = anyOrNull(),
            testsToSkip = any(), limit = anyOrNull(),
        )
    }

    @Test
    fun `given cached response, output file should contain same tests as first call`(): Unit = runBlocking {
        val tests = listOf(
            TestView(testDefinitionId = "id1", testPath = "com/example/FooTest", testName = "testFoo"),
        )
        whenever(
            metricsClient.getImpactedTests(
                groupId = any(), appId = any(),
                commitSha = anyOrNull(), buildVersion = anyOrNull(),
                baselineCommitSha = anyOrNull(), baselineBuildVersion = anyOrNull(),
                testsToSkip = any(), limit = anyOrNull()
            )
        ).thenReturn(tests)

        whenever(metricsClient.findBuild(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(BuildView("id", "myGroup", "myApp", null, "1.0.0"))

        val criteria = BuildVersionCriteria("1.0.0")

        service.getRecommendedTests(
            groupId = "myGroup", appId = "myApp", buildVersion = "2.0.0",
            baselineSearchStrategy = BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION,
            baselineSearchCriteria = criteria,
            outputPath = outputDir1,
        )

        service.getRecommendedTests(
            groupId = "myGroup", appId = "myApp", buildVersion = "2.0.0",
            baselineSearchStrategy = BaselineSearchStrategy.SEARCH_BY_BUILD_VERSION,
            baselineSearchCriteria = criteria,
            outputPath = outputDir2,
        )

        val file1 = java.io.File(outputDir1, "recommendedTests.json").readText()
        val file2 = java.io.File(outputDir2, "recommendedTests.json").readText()
        assertEquals(file1, file2, "Both modules should receive identical recommendedTests.json content")
    }
}


