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
    override fun getBuildComparisonReport(data: JsonObject): Report {
        val metrics = data["data"]?.jsonObject?.get("metrics")?.jsonObject

        val newMethods = metrics?.get("changes_new_methods")?.jsonPrimitive?.intOrNull ?: 0
        val modifiedMethods = metrics?.get("changes_modified_methods")?.jsonPrimitive?.intOrNull ?: 0
        val totalChanges = metrics?.get("total_changes")?.jsonPrimitive?.intOrNull ?: 0
        val testedChanges = metrics?.get("tested_changes")?.jsonPrimitive?.intOrNull ?: 0
        val coverage = metrics?.get("coverage")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val recommendedTests = metrics?.get("recommended_tests")?.jsonPrimitive?.intOrNull ?: 0

        val links = data["data"]?.jsonObject?.get("links")?.jsonObject
        val changesLink = links?.get("changes")?.jsonPrimitive?.contentOrNull ?: ""
        val recommendedTestsLink = links?.get("recommended_tests")?.jsonPrimitive?.contentOrNull ?: ""
        val fullReportLink = links?.get("full_report")?.jsonPrimitive?.contentOrNull ?: ""
        return Report(
            content = """
### Drill4J Bot - Change Testing Report            
Changes
[$totalChanges methods ($newMethods new, $modifiedMethods modified)]($changesLink)

Risks
[${totalChanges - testedChanges}/$totalChanges methods not tested]($changesLink)
[$coverage% coverage]($changesLink)

Recommended tests
[$recommendedTests tests]($recommendedTestsLink)            

[See details in Drill4J]($fullReportLink)             
            """.trimIndent(),
            format = ReportFormat.MARKDOWN
        )
    }
}