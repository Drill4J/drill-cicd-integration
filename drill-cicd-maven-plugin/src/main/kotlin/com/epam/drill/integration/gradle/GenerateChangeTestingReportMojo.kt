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

import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.service.ReportService
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import java.io.File


@Mojo(
    name = "generateChangeTestingReport",
    defaultPhase = LifecyclePhase.NONE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class GenerateChangeTestingReportMojo : AbstractDrillMojo() {

    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "baseline", required = true)
    var baseline: BaselineConfiguration? = null

    override fun execute() {
        val apiUrl = apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
        val apiKey = apiKey.fromEnv("DRILL_API_KEY")
        val groupId = groupId.required("groupId")
        val appId = appId.required("appId")
        val baselineSearchStrategy = baseline?.searchStrategy ?: BaselineSearchStrategy.SEARCH_BY_TAG
        val baselineTagPattern = baseline?.tagPattern ?: "*"
        val baselineTargetRef = baseline?.targetRef

        val reportService = ReportService(
            metricsClient = MetricsClientImpl(
                apiUrl = apiUrl,
                apiKey = apiKey
            ),
            gitClient = GitClientImpl(),
            reportGenerator = MarkdownReportGenerator()
        )
        val searchCriteria = when (baselineSearchStrategy) {
            BaselineSearchStrategy.SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
            BaselineSearchStrategy.SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("baseline.targetRef"))
        }

        log.info("Generating Drill4J Change Testing Report...")
        val reportPath = File(project.build?.directory, "/drill-reports").absolutePath
        runBlocking {
            reportService.generateChangeTestingReport(
                groupId = groupId,
                appId = appId,
                baselineSearchStrategy = baselineSearchStrategy,
                baselineSearchCriteria = searchCriteria,
                reportPath = reportPath
            )
        }
        log.info("Drill4J Change Testing Report generated at: $reportPath")
    }
}