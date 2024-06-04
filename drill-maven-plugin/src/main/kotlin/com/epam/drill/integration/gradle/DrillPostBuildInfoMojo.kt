package com.epam.drill.integration.gradle

import com.epam.drill.integration.common.client.BuildPayload
import com.epam.drill.integration.common.client.impl.DrillApiClientImpl
import com.epam.drill.integration.common.git.getGitBranch
import com.epam.drill.integration.common.git.getGitCommitInfo
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(
    name = "drillSendBuildInfo",
    defaultPhase = LifecyclePhase.INSTALL,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class DrillPostBuildInfoMojo : AbstractMojo() {

    @Parameter(property = "drillApiUrl", required = true)
    var drillApiUrl: String? = null

    @Parameter(property = "drillApiKey")
    var drillApiKey: String? = null

    @Parameter(property = "groupId", required = true)
    var groupId: String? = null

    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "buildVersion")
    var buildVersion: String? = null

    override fun execute() {
        val drillApiClient = DrillApiClientImpl(
            drillApiUrl = drillApiUrl.required("drillApiUrl"),
            drillApiKey = drillApiKey
        )

        val branch = getGitBranch()
        val commitInfo = getGitCommitInfo()
        val payload = BuildPayload(
            groupId = groupId.required("groupId"),
            appId = appId.required("appId"),
            buildVersion = buildVersion,
            commitSha = commitInfo.sha,
            commitDate = commitInfo.date,
            commitAuthor = commitInfo.author,
            commitMessage = commitInfo.message,
            branch = branch
        )

        runBlocking {
            drillApiClient.sendBuild(payload)
        }
    }
}