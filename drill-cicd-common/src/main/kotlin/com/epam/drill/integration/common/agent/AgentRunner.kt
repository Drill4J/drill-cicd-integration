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
                FileUrl(downloadUrl, "$currentOsPreset.zip"),
                distDir,
                agentArgs
            )

            version != null -> return getJvmOptionsByVersion(
                configuration.githubRepository,
                version,
                distDir,
                agentArgs
            )

            else -> throw IllegalStateException("You must specify either the agent version, or the downloadUrl, or the zipPath")
        }
    }

    private suspend fun getJvmOptionsByVersion(
        repositoryName: String,
        releaseVersion: String,
        distDir: Directory,
        agentArgs: Map<String, String?>
    ): List<String> = agentInstaller.getDownloadUrl(
        repositoryName,
        releaseVersion,
        currentOsPreset
    )?.let { fileUrl ->
        getJvmOptionsByDownloadUrl(fileUrl, distDir, agentArgs)
    }
        ?: throw IllegalStateException("Can't find the agent release for repository $repositoryName and version $releaseVersion")


    private fun getJvmOptionsByUnzippedDir(
        unzippedDir: Directory,
        agentArgs: Map<String, String?>
    ): List<String> = agentInstaller.findAgentFile(
        unzippedDir,
        currentOsLibExt
    )?.let { agentFile ->
        getJvmOptionsByAgentFile(agentFile, agentArgs)
    } ?: throw IllegalStateException("Could not find agent .$currentOsLibExt file in $unzippedDir.")

    private suspend fun getJvmOptionsByDownloadUrl(
        archiveFileUrl: FileUrl,
        distDir: Directory,
        agentArgs: Map<String, String?>
    ): List<String> = agentInstaller.download(
        archiveFileUrl,
        distDir
    ).let { archiveFile ->
        agentInstaller.unzip(archiveFile)
    }.let { unzippedDir ->
        getJvmOptionsByUnzippedDir(unzippedDir, agentArgs)
    }

    private fun getJvmOptionsByZipFile(
        zipFile: File,
        destinationDir: Directory,
        agentArgs: Map<String, String?>
    ) = agentInstaller.unzip(
        zipFile,
        destinationDir
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

