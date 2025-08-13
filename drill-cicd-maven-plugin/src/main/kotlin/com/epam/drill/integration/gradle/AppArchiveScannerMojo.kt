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

import com.epam.drill.integration.common.agent.CommandExecutor
import com.epam.drill.integration.common.agent.ExecutableRunner
import com.epam.drill.integration.common.agent.config.AppAgentConfiguration
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.agent.impl.JarCommandLineBuilder
import com.epam.drill.integration.common.agent.javaExecutable
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import java.io.File

@Mojo(
    name = "scanAppArchive",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class AppArchiveScannerMojo : AbstractDrillMojo() {

    @Parameter(property = "appAgent", required = true)
    var appAgent: AppAgentMavenConfiguration? = null

    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "packagePrefixes", required = true)
    var packagePrefixes: String? = null

    @Parameter(property = "buildVersion", required = false)
    var buildVersion: String? = null

    private val gitClient = GitClientImpl()
    private val agentCache = AgentCacheImpl(drillAgentFilesDir)
    private val agentInstaller = AgentInstallerImpl(agentCache)
    private val argumentsBuilder = JarCommandLineBuilder()
    private val commandExecutor = CommandExecutor(javaExecutable.absolutePath)
    private val executableRunner = ExecutableRunner(agentInstaller, argumentsBuilder, commandExecutor)

    override fun execute() {
        if (project.packaging == "pom") {
            log.warn("The 'pom' packaging is not supported for the App Archive Scanner. " +
                    "Please use 'jar' or 'war' packaging.")
            return
        }
        val archiveFile = project.artifact.file ?: File(project.build.directory, project.build.finalName + "." + project.packaging)
        if (!archiveFile.exists()) {
            log.error("The archive file '${archiveFile.absolutePath}' does not exist. " +
                    "Please ensure the project is built before running the App Archive Scanner.")
            return
        }
        log.info("App archive scanner running for ${archiveFile.absolutePath}...")
        val distDir = File(project.build?.directory, "/drill")
        val config = getConfig(archiveFile)
        runBlocking {
            log.info("App archive scanner running for file ${archiveFile.absolutePath} ...")
            executableRunner.runScan(config, distDir) { line ->
                log.info(line)
            }.also { exitCode ->
                log.info("App archive scanner exited with code $exitCode")
            }
        }
    }

    private fun getConfig(archiveFile: File) = AppAgentConfiguration().apply {
        val mavenConfig = this@AppArchiveScannerMojo
        val appAgent = mavenConfig.appAgent.required("appAgent")

        setGeneralAgentProperties(appAgent, mavenConfig)
        appId = mavenConfig.appId.required("appId")
        packagePrefixes = mavenConfig.packagePrefixes.required("packagePrefixes")
            .split(*arrayOf(",", ";"))
            .map(String::trim)
            .toTypedArray()
        scanClassPath = archiveFile.absolutePath
        buildVersion = mavenConfig.buildVersion
        envId = mavenConfig.appAgent?.envId
        commitSha = runCatching {
            gitClient.getCurrentCommitSha()
        }.onFailure {
            log.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
        }.getOrNull()
    }
}