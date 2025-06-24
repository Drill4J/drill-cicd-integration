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

import com.epam.drill.integration.common.agent.ExecutableRunner
import com.epam.drill.integration.common.agent.config.AppArchiveScannerConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.fromEnv
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import java.io.File

@Mojo(
    name = "enableAppArchiveScanner",
    defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true
)
class AppArchiveScannerMojo : AbstractDrillMojo() {

    @Parameter(property = "appArchiveScanner", required = true)
    var appArchiveScanner: AppArchiveScannerMavenConfiguration? = null

    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "packagePrefixes", required = true)
    var packagePrefixes: Array<String>? = null

    @Parameter(property = "buildVersion", required = false)
    var buildVersion: String? = null

    private val gitClient = GitClientImpl()
    private val agentCache = AgentCacheImpl(drillAgentFilesDir)
    private val agentInstaller = AgentInstallerImpl(agentCache)
    private val executableRunner = ExecutableRunner(agentInstaller)

    override fun execute() {
        println("App archive scanner running...")
        val distDir = File(project.build?.directory, "/drill")
        val config = getConfig()
        val appArchive = project.artifact.file
        runBlocking {
            val exitCode = executableRunner.runScan(
                config,
                distDir,
                appArchive.absolutePath
            ) { line ->
                println(line)
            }
            println("App archive scanner exited with code $exitCode")
        }
    }

    private fun getConfig() = AppArchiveScannerConfiguration().apply {
        val mavenConfig = this@AppArchiveScannerMojo
        val appArchiveScanner = mavenConfig.appArchiveScanner.required("appArchiveScanner")

        setGeneralAgentProperties(appArchiveScanner, mavenConfig)
        appId = mavenConfig.appId.required("appId")
        packagePrefixes = mavenConfig.packagePrefixes.required("packagePrefixes")

        // TODO additional params - scanClassPath
//        additionalParams = mapOf(
//            "scanClassPath" to "${project.build.outputDirectory};!${project.build.testOutputDirectory}"
//        ) + (additionalParams ?: emptyMap())
        buildVersion = mavenConfig.buildVersion
        commitSha = runCatching {
            gitClient.getCurrentCommitSha()
        }.onFailure {
            log.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
        }.getOrNull()
    }
}


fun AppArchiveScannerConfiguration.setGeneralAgentProperties(
    appArchiveScannerMavenConfiguration: AppArchiveScannerMavenConfiguration,
    mavenGeneralConfig: AbstractDrillMojo
) {
    version = appArchiveScannerMavenConfiguration.version
    downloadUrl = appArchiveScannerMavenConfiguration.downloadUrl
    zipPath = appArchiveScannerMavenConfiguration.zipPath?.let { File(it) }

    apiUrl = mavenGeneralConfig.apiUrl.fromEnv("DRILL_API_URL").required("apiUrl")
    apiKey = mavenGeneralConfig.apiKey.fromEnv("DRILL_API_KEY")
    groupId = mavenGeneralConfig.groupId.required("groupId")

    // TODO additional params - scanClassPath
//    additionalParams = appArchiveScannerMavenConfiguration.additionalParams
}