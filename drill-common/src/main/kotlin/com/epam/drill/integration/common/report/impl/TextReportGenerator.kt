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

import com.epam.drill.integration.common.report.ReportFormat
import com.epam.drill.integration.common.report.ReportGenerator
import kotlinx.serialization.json.JsonObject

class TextReportGenerator : ReportGenerator {
    override fun getDiffSummaryReport(metrics: JsonObject) =
        """
            Drill4J CI/CD report:
            - Coverage: ${metrics["coverage"]}%
            - Risks: ${metrics["risks"]}
        """.trimIndent()

    override fun getFormat() = ReportFormat.PLAINTEXT
}