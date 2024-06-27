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
package com.epam.drill.integration.gradle

import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_MERGE_BASE
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_TAG
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.service.ReportService
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.gradle.api.Task
import java.io.File

fun Task.drillGenerateChangeTestingReport(config: DrillExtension) {
    doFirst {
        val drillApiUrl = config.drillApiUrl.required("drillApiUrl")
        val drillApiKey = config.drillApiKey
        val groupId = config.groupId.required("groupId")
        val appId = config.appId.required("appId")
        val baselineSearchStrategy = config.baseline?.searchStrategy ?: SEARCH_BY_TAG
        val baselineTagPattern = config.baseline?.tagPattern ?: "*"
        val baselineTargetRef = config.baseline?.targetRef

        val reportService = ReportService(
            metricsClient = MetricsClientImpl(
                drillApiUrl = drillApiUrl,
                drillApiKey = drillApiKey
            ),
            gitClient = GitClientImpl(),
            reportGenerator = MarkdownReportGenerator()
        )
        val searchCriteria = when (baselineSearchStrategy) {
            SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
            SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baseline.targetRef"))
        }

        logger.lifecycle("Generating Drill4J Change Testing Report...")
        runBlocking {
            reportService.generateChangeTestingReport(
                groupId = groupId,
                appId = appId,
                baselineSearchStrategy = baselineSearchStrategy,
                baselineSearchCriteria = searchCriteria,
                reportPath = File(project.buildDir, "/reports/drill").absolutePath
            )
        }
    }
}