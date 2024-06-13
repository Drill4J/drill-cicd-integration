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
        val inputParameters = data["data"]?.jsonObject?.get("inputParameters")?.jsonObject
        val groupId = inputParameters?.get("groupId")?.jsonPrimitive?.contentOrNull
        val appId = inputParameters?.get("appId")?.jsonPrimitive?.contentOrNull
        val commitSha = inputParameters?.get("commitSha")?.jsonPrimitive?.contentOrNull
        val baselineCommitSha = inputParameters?.get("baselineCommitSha")?.jsonPrimitive?.contentOrNull

        val metrics = data["data"]?.jsonObject?.get("metrics")?.jsonObject
        val newMethods = metrics?.get("changes_new_methods")?.jsonPrimitive?.intOrNull ?: 0
        val modifiedMethods = metrics?.get("changes_modified_methods")?.jsonPrimitive?.intOrNull ?: 0
        val totalChanges = metrics?.get("total_changes")?.jsonPrimitive?.intOrNull ?: 0
        val testedChanges = metrics?.get("tested_changes")?.jsonPrimitive?.intOrNull ?: 0
        val coverage = metrics?.get("coverage")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val recommendedTests = metrics?.get("recommended_tests")?.jsonPrimitive?.intOrNull ?: 0

        val links = data["data"]?.jsonObject?.get("links")?.jsonObject
        val buildLink = links?.get("build")?.jsonPrimitive?.contentOrNull
        val baselineBuildLink = links?.get("baseline_build")?.jsonPrimitive?.contentOrNull
        val changesLink = links?.get("changes")?.jsonPrimitive?.contentOrNull
        val recommendedTestsLink = links?.get("recommended_tests")?.jsonPrimitive?.contentOrNull
        val fullReportLink = links?.get("full_report")?.jsonPrimitive?.contentOrNull

        val descriptionText = "Comparing ${commitSha?.shortSha()?.wrapToLink(buildLink)} (current) " +
                "to ${baselineCommitSha?.shortSha()?.wrapToLink(baselineBuildLink)} (baseline)."
        val changesText = "$totalChanges method${totalChanges.pluralEnding("s")} ($newMethods new, $modifiedMethods modified)"
            .takeIf { totalChanges > 0 }
            ?.wrapToLink(changesLink)
            ?: "No changes detected"
        val testedMethodsText = "${totalChanges - testedChanges}/$totalChanges methods not tested"
            .takeIf { totalChanges - testedChanges > 0 }
            ?.wrapToLink(changesLink)
            ?: "All changes tested".wrapToLink(changesLink)
        val coverageText = "${coverage.percent()}% coverage"
            .wrapToLink(changesLink)
        val recommendedTestsText = "$recommendedTests test${recommendedTests.pluralEnding("s")}"
            .takeIf { recommendedTests > 0 }
            ?.wrapToLink(recommendedTestsLink)
            ?: "None"
        val seeDetailsText = "See details in Drill4J"
            .takeIf { fullReportLink != null }
            ?.wrapToLink(fullReportLink)
            ?: ""

        val reportHeader = """
### Drill4J Bot - Change Testing Report


            """.trimIndent()

        val descriptionParagraph = """
$descriptionText


            """.trimIndent()

        val changesParagraph = """
**Changes**
$changesText   


            """.trimIndent()

        val risksParagraph = """
**Risks**
$testedMethodsText
$coverageText


            """.trimIndent()
            .takeIf { totalChanges > 0 }
            ?: ""

        val recommendedTestsParagraph = """
**Recommended tests**
$recommendedTestsText


""".trimIndent()

        val seeDetailsParagraph = """
$seeDetailsText            
""".trimIndent()

        return Report(
            content = reportHeader
                    + descriptionParagraph
                    + changesParagraph
                    + risksParagraph
                    + recommendedTestsParagraph
                    + seeDetailsParagraph,
            format = ReportFormat.MARKDOWN
        )
    }
}