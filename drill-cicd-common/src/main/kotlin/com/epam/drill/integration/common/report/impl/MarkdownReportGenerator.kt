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
        val inputParameters = data["data"]?.jsonObject?.get("inputParameters") as? JsonObject
        val groupId = inputParameters?.get("groupId")?.jsonPrimitive?.contentOrNull
        val appId = inputParameters?.get("appId")?.jsonPrimitive?.contentOrNull
        val commitSha = inputParameters?.get("commitSha")?.jsonPrimitive?.contentOrNull
        val baselineCommitSha = inputParameters?.get("baselineCommitSha")?.jsonPrimitive?.contentOrNull

        val metrics = data["data"]?.jsonObject?.get("metrics") as? JsonObject
        val newMethods = metrics?.get("changes_new_methods")?.jsonPrimitive?.intOrNull ?: 0
        val modifiedMethods = metrics?.get("changes_modified_methods")?.jsonPrimitive?.intOrNull ?: 0
        val deletedMethods = metrics?.get("changes_deleted_methods")?.jsonPrimitive?.intOrNull ?: 0
        val testedNewMethods = metrics?.get("tested_new_methods")?.jsonPrimitive?.intOrNull ?: 0
        val testedModifiedMethods = metrics?.get("tested_modified_methods")?.jsonPrimitive?.intOrNull ?: 0
        val totalChanges = newMethods + modifiedMethods + deletedMethods
        val riskChanges = newMethods + modifiedMethods
        val testedChanges = metrics?.get("tested_changes")?.jsonPrimitive?.intOrNull ?: 0
        val coverage = metrics?.get("coverage")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val impactedTests = metrics?.get("impacted_tests")?.jsonPrimitive?.intOrNull ?: 0

        val links = data["data"]?.jsonObject?.get("links") as? JsonObject
        val buildLink = links?.get("build")?.jsonPrimitive?.contentOrNull
        val baselineBuildLink = links?.get("baseline_build")?.jsonPrimitive?.contentOrNull
        val changesLink = links?.get("changes")?.jsonPrimitive?.contentOrNull
        val impactedTestsLink = links?.get("impacted_tests")?.jsonPrimitive?.contentOrNull
        val fullReportLink = links?.get("full_report")?.jsonPrimitive?.contentOrNull

        val descriptionText = "Comparing ${commitSha?.shortSha()?.wrapToLink(buildLink)} (current) " +
                "to ${baselineCommitSha?.shortSha()?.wrapToLink(baselineBuildLink)} (baseline)."

        val newText = "$newMethods new (${testedNewMethods}/$newMethods tested)"
        val modifiedText =
            "$modifiedMethods modified (${testedModifiedMethods}/$modifiedMethods tested)"
        val deletedText = "$deletedMethods deleted"

        val coverageText = "${coverage.percent()}% of changes covered".wrapToLink(changesLink)

        val impactedTestsText = "$impactedTests test${impactedTests.pluralEnding("s")}"
            .takeIf { impactedTests > 0 }
            ?.wrapToLink(impactedTestsLink)
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
$newText   
$modifiedText
$deletedText


            """.trimIndent().takeIf { totalChanges > 0 } ?: """
**Changes**
No changes detected


            """.trimIndent()

        val coverageParagraph = """
**Coverage**
$coverageText


            """.trimIndent()
            .takeIf { totalChanges > 0 }
            ?: ""

        val recommendedTestsParagraph = """
**Impacted tests**
$impactedTestsText


""".trimIndent()

        val seeDetailsParagraph = """
$seeDetailsText            
""".trimIndent()

        return Report(
            content = reportHeader
                    + descriptionParagraph
                    + changesParagraph
                    + coverageParagraph
                    + recommendedTestsParagraph
                    + seeDetailsParagraph,
            format = ReportFormat.MARKDOWN
        )
    }
}