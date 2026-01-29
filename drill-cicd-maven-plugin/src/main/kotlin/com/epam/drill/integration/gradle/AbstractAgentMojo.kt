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
import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.agent.config.AgentMode
import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import com.epam.drill.integration.common.agent.impl.AgentInstallerImpl
import com.epam.drill.integration.common.agent.impl.JarCommandLineBuilder
import com.epam.drill.integration.common.agent.impl.JavaAgentCommandLineBuilder
import com.epam.drill.integration.common.agent.impl.NativeAgentCommandLineBuilder
import com.epam.drill.integration.common.agent.javaExecutable
import com.epam.drill.integration.common.baseline.BaselineFactory
import com.epam.drill.integration.common.baseline.BaselineSearchStrategy
import com.epam.drill.integration.common.baseline.MergeBaseCriteria
import com.epam.drill.integration.common.baseline.TagCriteria
import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.getJavaAddOpensOptions
import com.epam.drill.integration.common.util.required
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import kotlin.collections.joinToString


val drillAgentFilesDir = File(System.getProperty("user.home"), ".drill/agents")
private const val ARG_LINE = "argLine"

abstract class AbstractAgentMojo: AbstractDrillMojo() {
    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "packagePrefixes", required = true)
    var packagePrefixes: String? = null

    @Parameter(property = "buildVersion", required = false)
    var buildVersion: String? = null

    @Parameter(property = "envId", required = false)
    var envId: String? = null

    @Parameter(property = "testTaskId", required = false)
    var testTaskId: String? = null

    @Parameter(property = "agent", required = true)
    var agent: AgentMavenConfiguration? = null

    @Parameter(property = "classScanning", required = false)
    var classScanning: ClassScanningConfiguration? = null

    @Parameter(property = "baseline", required = true)
    var baseline: BaselineConfiguration? = null

    @Parameter(property = "additionalParams", required = false)
    var additionalParams: Map<String, String>? = null


    protected val agentCache = AgentCacheImpl(drillAgentFilesDir)
    protected val agentInstaller = AgentInstallerImpl(agentCache)
    protected val gitClient = GitClientImpl()
    protected val argumentsBuilder = JarCommandLineBuilder()
    protected val commandExecutor = CommandExecutor(javaExecutable.absolutePath)
    protected val executableRunner = ExecutableRunner(agentInstaller, argumentsBuilder, commandExecutor)
    protected val baselineFactory = BaselineFactory(gitClient)

    abstract fun getAgentConfig(): AgentConfiguration

    override fun execute() {
        val distDir = File(project.build?.directory, "/drill").absolutePath
        val config = getAgentConfig()
        val jvmOptions = runBlocking {
            agentInstaller.installAgent(
                File(distDir, config.agentName),
                config
            )
        }.let { agentDir ->
            when (config.agentMode) {
                AgentMode.NATIVE -> NativeAgentCommandLineBuilder()
                AgentMode.JAVA -> JavaAgentCommandLineBuilder()
            }.build(agentDir, config)
        }

        val oldArgLine = project.properties.getProperty(ARG_LINE) ?: ""
        val javaAddOpensOptions = getJavaAddOpensOptions().joinToString(separator = " ")
        val newArgLine = "$oldArgLine ${jvmOptions.joinToString(" ")} $javaAddOpensOptions".trim()
        project.properties.setProperty(ARG_LINE, newArgLine)
        log.info("JVM args $jvmOptions have been added to the goal")
    }
}

internal fun AgentConfiguration.mapGeneralAgentProperties(
    config: AbstractAgentMojo
) {
    val agentConfig = config.agent.required("agent")

    agentConfig.takeIf {
        it.version != null || it.zipPath != null || it.downloadUrl != null
    }?.let { agentExtension ->
        this.version = agentExtension.version
        this.downloadUrl = agentExtension.downloadUrl
        this.zipPath = agentExtension.zipPath?.let { File(it) }
    }

    this.logLevel = agentConfig.logLevel
    this.logFile = (agentConfig.logFile)?.let { File(it) }
    this.agentMode = (agentConfig.agentMode)?.let { AgentMode.valueOf(it) } ?: AgentMode.NATIVE

    this.apiUrl = config.apiUrl
    this.apiKey = config.apiKey
    this.groupId = config.groupId
    this.appId = config.appId
    this.envId = config.envId
    this.packagePrefixes = config.packagePrefixes?.split(",")?.map { it.trim() }?.toTypedArray() ?: emptyArray()
    this.buildVersion = config.buildVersion
    this.additionalParams = config.additionalParams
}

internal fun AgentConfiguration.mapBuildSpecificProperties(
    config: AbstractAgentMojo,
    log: Log,
    gitClient: GitClient
) {
    this.buildVersion = config.buildVersion
    this.commitSha = runCatching {
        gitClient.getCurrentCommitSha()
    }.onFailure {
        log.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
    }.getOrNull()
}

internal fun AgentConfiguration.mapClassScanningProperties(
    config: AbstractAgentMojo,
    project: MavenProject,
    archiveFile: File?
) {
    val appClasses = config.classScanning?.appClasses ?: archiveFile?.absolutePath?.let { listOf(it) } ?: listOf(project.build.outputDirectory)
    val testClasses = config.classScanning?.testClasses ?: listOf(project.build.testOutputDirectory)
    this.scanClassPath = (appClasses + testClasses.map { "!$it" }).joinToString(separator = ";")
    this.classScanningEnabled = (config.classScanning?.enabled ?: false) && (config.classScanning?.runtime ?: false)
}

