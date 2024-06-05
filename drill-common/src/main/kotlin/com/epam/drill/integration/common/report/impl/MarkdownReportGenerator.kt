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
package com.epam.drill.integration.common.report.impl

import com.epam.drill.integration.common.report.Report
import com.epam.drill.integration.common.report.ReportFormat
import com.epam.drill.integration.common.report.ReportGenerator
import kotlinx.serialization.json.*

class MarkdownReportGenerator : ReportGenerator {
    override fun getBuildComparisonReport(metrics: JsonObject): Report {
        val data = metrics["data"]?.jsonObject?.get("metrics")!!.jsonObject

        val newMethods = data["changes_new_methods"]?.jsonPrimitive?.contentOrNull ?: "0"
        val modifiedMethods = data["changes_modified_methods"]?.jsonPrimitive?.contentOrNull ?: "0"
        val totalChanges = data["total_changes"]?.jsonPrimitive?.contentOrNull ?: "0"
        val testedChanges = data["tested_changes"]?.jsonPrimitive?.contentOrNull ?: "0"
        val coverage = data["coverage"]?.jsonPrimitive?.contentOrNull ?: "0"
        val recommendedTests = data["recommended_tests"]?.jsonPrimitive?.contentOrNull ?: "0"
        val changesLink = "https://drill4j.github.io/"
        val risksLink = "https://drill4j.github.io/"
        val recommendedTestsLink = "https://drill4j.github.io/"
        val fullReportLink = "https://drill4j.github.io/"
        return Report(
            content = """
### Drill4J Bot - Change Testing Report            
Changes
[$totalChanges methods ($newMethods new, $modifiedMethods modified)]($changesLink)

Tested changes
[$testedChanges$/$totalChanges methods tested]($risksLink)
[$coverage% coverage]($risksLink)

Recommended tests
[$recommendedTests tests]($recommendedTestsLink)            

[See details on Drill4J]($fullReportLink)             
            """.trimIndent(),
            format = ReportFormat.MARKDOWN
        )
    }
}