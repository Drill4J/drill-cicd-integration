package com.epam.drill.integration.cli

import com.epam.drill.integration.common.client.BuildPayload
import com.epam.drill.integration.common.client.impl.DataIngestClientImpl
import com.epam.drill.integration.common.client.impl.MetricsClientImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.report.impl.MarkdownReportGenerator
import com.epam.drill.integration.common.service.ReportService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking

class GenerateChangeTestingReportCommand : CliktCommand(name = "sendBuildInfo") {
    private val drillApiUrl by option("-drill-u", "--drillApiUrl", envvar = "DRILL_API_URL").required()
    private val drillApiKey by option("-drill-k", "--drillApiKey", envvar = "DRILL_API_KEY")
    private val groupId by option("-g", "--groupId", envvar = "DRILL_GROUP_ID").required()
    private val appId by option("-a", "--appId", envvar = "DRILL_APP_ID").required()
    private val tagPattern by option("-t", "--tagPattern").default("*")

    override fun run() {
        val reportService = ReportService(
            metricsClient = MetricsClientImpl(
                drillApiUrl = drillApiUrl,
                drillApiKey = drillApiKey
            ),
            gitClient = GitClientImpl(),
            reportGenerator = MarkdownReportGenerator()
        )

        echo("Generating Drill4J Testing Report for $groupId/$appId comparing with tag pattern $tagPattern...")
        runBlocking {
            reportService.generateChangeTestingReportByTag(
                groupId = groupId,
                appId = appId,
                tagPattern = tagPattern
            )
        }
        echo("Done.")
    }

}