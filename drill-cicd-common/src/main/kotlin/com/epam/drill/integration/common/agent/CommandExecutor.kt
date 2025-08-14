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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging

class CommandExecutor(
    private val command: String,
    private val workingDir: String? = null,
    private val env: Map<String, String>? = null
) {
    private val logger = KotlinLogging.logger {}

    suspend fun execute(
        args: List<String>,
        onOutputLine: suspend (String) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        logger.debug("Executing command: $command ${args.joinToString(" ")}")
        val processBuilder = ProcessBuilder(listOf(command) + args)
        if (workingDir != null) {
            processBuilder.directory(java.io.File(workingDir))
        }
        if (env != null) {
            processBuilder.environment().putAll(env)
        }
        processBuilder.redirectErrorStream(true)
        val process = processBuilder.start()

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
}
