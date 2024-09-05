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
package com.epam.drill.integration.cli

import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_MERGE_BASE
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy.SEARCH_BY_TAG
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.service.ReportService
import com.epam.drill.integration.common.util.required
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class GenerateChangeTestingReportCommand : CliktCommand(name = "generateChangeTestingReport") {
    private val apiUrl by option("-drill-u", "--apiUrl", envvar = "DRILL_API_URL").required()
    private val apiKey by option("-drill-k", "--apiKey", envvar = "DRILL_API_KEY")
    private val groupId by option("-g", "--groupId", envvar = "DRILL_GROUP_ID").required()
    private val appId by option("-a", "--appId", envvar = "DRILL_APP_ID").required()

    private val baselineSearchStrategyName by option("-bl-s", "--baselineSearchStrategy").default(SEARCH_BY_TAG.name)
    private val baselineTagPattern by option("-bl-t", "--baselineTagPattern").default("*")
    private val baselineTargetRef by option("-bl-tr", "--baselineTargetRef")

    override fun run() {
        val reportService = ReportService(
            metricsClient = MetricsClientImpl(
                apiUrl = apiUrl,
                apiKey = apiKey
            ),
            gitClient = GitClientImpl(),
            reportGenerator = MarkdownReportGenerator()
        )
        val searchStrategy = BaselineSearchStrategy.valueOf(baselineSearchStrategyName)
        val searchCriteria = when (searchStrategy) {
            SEARCH_BY_TAG -> TagCriteria(baselineTagPattern)
            SEARCH_BY_MERGE_BASE -> MergeBaseCriteria(baselineTargetRef.required("--baselineTargetRef"))
        }

        echo("Generating Drill4J Testing Report...")
        runBlocking {
            reportService.generateChangeTestingReport(
                groupId = groupId,
                appId = appId,
                baselineSearchStrategy = searchStrategy,
                baselineSearchCriteria = searchCriteria
            )
        }
        echo("Done.")
    }

}