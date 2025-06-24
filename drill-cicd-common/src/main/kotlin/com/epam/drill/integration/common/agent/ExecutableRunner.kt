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

import com.epam.drill.integration.common.agent.config.AppArchiveScannerConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ExecutableRunner(
    private val agentInstaller: AgentInstaller,
    scannerRunnerParam: (suspend (List<String>, String, suspend (String) -> Unit) -> Int)? = null
) {

    private val scannerRunner = scannerRunnerParam ?: ::runScannerAsync

    suspend fun resolveZipFile(configuration: AppArchiveScannerConfiguration): File {
        configuration.zipPath?.takeIf { it.exists() }?.let {
            return it
        }

        configuration.downloadUrl?.takeIf { it.isNotBlank() }?.let {
            return agentInstaller.downloadByUrl(it, configuration.agentName)
        }

        configuration.version?.takeIf { it.isNotBlank() }?.let {
            return agentInstaller.downloadByVersion(
                configuration.githubRepository,
                configuration.agentName,
                it
            )
        }

        throw IllegalStateException(
            "Could not download or find app archive scanner zip. " +
                    "Specify either of parameters: version, downloadUrl, zipPath"
        )
    }

    fun buildCommandLine(executable: File, configuration: AppArchiveScannerConfiguration): List<String> {
        val args = configuration.toAgentArguments()
            .filter { !it.value.isNullOrEmpty() }
            .flatMap { (k, v) -> listOf(toCliArg(k), v!!) }

        return listOf(executable.absolutePath) + args
    }

    fun extractExecutable(zipFile: File, distDir: Directory): File {
        val unzippedDir = agentInstaller.unzip(zipFile, distDir)
        return unzippedDir.listFiles()
            ?.firstOrNull { it.isFile && it.canExecute() }
            ?: throw IllegalStateException("Could not find app archive scanner executable in ${unzippedDir.absolutePath}")
    }

    suspend fun runScannerAsync(
        argLine: List<String>,
        archive: String,
        onOutputLine: suspend (String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            *(argLine + listOf("--scanClassPath", archive)).toTypedArray()
        )
            .redirectErrorStream(true)
            .start()

        val reader = process.inputStream.bufferedReader()

        try {
            reader.lineSequence().forEach { line ->
                onOutputLine(line)
            }
        } finally {
            reader.close()
        }

        process.waitFor()
    }

    suspend fun runScan(
        config: AppArchiveScannerConfiguration,
        distDir: Directory,
        archive: String,
        onOutputLine: suspend (String) -> Unit
    ): Int {
        val zipFile = resolveZipFile(config)
        val executable = extractExecutable(zipFile, distDir)
        val cmd = buildCommandLine(executable, config)
        return scannerRunner(cmd, archive, onOutputLine)
    }
}

private fun toCliArg(input: String): String = "--$input"
