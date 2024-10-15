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
package com.epam.drill.integration.common.agent

import com.epam.drill.integration.common.agent.config.AgentConfiguration
import java.io.File

class AgentRunner(
    private val agentInstaller: AgentInstaller
) {
    suspend fun getJvmOptionsToRun(
        distDir: Directory,
        configuration: AgentConfiguration
    ): List<String> {
        val agentArgs = configuration.toAgentArguments().filter { !it.value.isNullOrEmpty() }
        val zipFile = configuration.zipPath
        val downloadUrl = configuration.downloadUrl
        val version = configuration.version

        when {
            zipFile != null -> return getJvmOptionsByZipFile(
                zipFile,
                distDir,
                agentArgs
            )

            downloadUrl != null -> return getJvmOptionsByDownloadUrl(
                configuration.agentName,
                downloadUrl,
                distDir,
                agentArgs
            )

            version != null -> return getJvmOptionsByVersion(
                configuration.agentName,
                configuration.githubRepository,
                version,
                distDir,
                agentArgs
            )

            else -> throw IllegalStateException("You must specify either the agent version, or the downloadUrl, or the zipPath")
        }
    }

    private fun getJvmOptionsByUnzippedDir(
        unzippedDir: Directory,
        agentArgs: Map<String, String?>
    ): List<String> = agentInstaller.findAgentFile(
        unzippedDir,
        currentOsLibExt
    )?.let { agentFile ->
        getJvmOptionsByAgentFile(agentFile, agentArgs)
    } ?: throw IllegalStateException("Could not find agent .$currentOsLibExt file in $unzippedDir.")


    private suspend fun getJvmOptionsByVersion(
        agentName: String,
        repositoryName: String,
        version: String,
        distDir: Directory,
        agentArgs: Map<String, String?>
    ): List<String> = run {
        agentInstaller.downloadByVersion(
            repositoryName,
            agentName,
            version,
        )
    }.let { zipFile ->
        agentInstaller.unzip(
            zipFile,
            distDir
        )
    }.let { unzippedDir ->
        getJvmOptionsByUnzippedDir(unzippedDir, agentArgs)
    }

    private suspend fun getJvmOptionsByDownloadUrl(
        agentName: String,
        downloadUrl: String,
        distDir: Directory,
        agentArgs: Map<String, String?>
    ): List<String> = run {
        agentInstaller.downloadByUrl(
            downloadUrl,
            agentName
        )
    }.let { zipFile ->
        agentInstaller.unzip(
            zipFile,
            distDir
        )
    }.let { unzippedDir ->
        getJvmOptionsByUnzippedDir(unzippedDir, agentArgs)
    }

    private fun getJvmOptionsByZipFile(
        zipFile: File,
        distDir: Directory,
        agentArgs: Map<String, String?>
    ) = agentInstaller.unzip(
        zipFile,
        distDir
    ).let { unzippedDir ->
        getJvmOptionsByUnzippedDir(unzippedDir, agentArgs)
    }

    private fun getJvmOptionsByAgentFile(
        agentFile: File,
        agentArgs: Map<String, String?>
    ) = listOf(
        "-agentpath:${agentFile.absolutePath}="
                + "drillInstallationDir=${agentFile.parent ?: ""},"
                + agentArgs.map { (k, v) -> "$k=$v" }.joinToString(",")
    )
}

