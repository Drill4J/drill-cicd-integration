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

import com.epam.drill.integration.common.agent.config.AgentConfiguration
import kotlinx.coroutines.runBlocking
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.ResolutionScope
import java.io.File

@Mojo(
    name = "scanAppArchive",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    threadSafe = true
)
class AppArchiveScannerMojo : AbstractAgentMojo() {

    override fun getAgentConfig() = AgentConfiguration().apply {
        val config = this@AppArchiveScannerMojo
        mapGeneralAgentProperties(config)
        mapBuildSpecificProperties(config, log, gitClient)
        mapClassScanningProperties(config, project, mojoExecution.lifecyclePhase, log, archiveFile, true)
        this.messageSendingMode = "DIRECT"
        if (this.scanClassPath?.isEmpty() ?: true) {
            throw IllegalStateException("No classes or archives to scan for Drill4J Agent.")
        }
    }

    override fun execute() {
        if (project.packaging == "pom") {
            log.warn("The 'pom' packaging is not supported for Drill4J Class Scanner. " +
                    "Please use 'jar' or 'war' packaging.")
            return
        }
        val distDir = File(project.build?.directory, "/drill")
        val config = getAgentConfig()
        runBlocking {
            log.info("Drill4J class scanner is running...")
            log.info("Scanning: ${config.scanClassPath}")
            executableRunner.runScan(config, distDir) { line ->
                log.info(line)
            }.also { exitCode ->
                log.info("Drill4J class scanner exited with code $exitCode")
            }
        }
    }

    private val archiveFile: File?
        get() {
            val archiveFile = project.artifact.file ?: File(project.build.directory, project.build.finalName + "." + project.packaging)
            if (!archiveFile.exists()) {
                log.error("The archive file '${archiveFile.absolutePath}' does not exist. " +
                        "Please ensure the project is built before running Drill4J Class Scanner.")
                return null
            }
            return archiveFile
        }
}