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
package com.epam.drill.integration.common.agent.impl

import com.epam.drill.integration.common.agent.CommandLineBuilder
import com.epam.drill.integration.common.agent.Directory
import com.epam.drill.integration.common.agent.config.AgentConfiguration
import com.epam.drill.integration.common.agent.currentOsLibExt
import java.io.File

class NativeAgentCommandLineBuilder : CommandLineBuilder {
    override fun build(
        agentDir: Directory,
        configuration: AgentConfiguration
    ): List<String> {
        val nativeAgentFile = findNativeAgentFile(agentDir)
        return listOf(
            "-agentpath:${nativeAgentFile.absolutePath}="
                    + "drillInstallationDir=${nativeAgentFile.parent ?: ""},"
                    + getArgsMap(configuration).map { (k, v) -> "$k=$v" }.joinToString(",")
        )
    }

    private fun findNativeAgentFile(agentDir: Directory): File {
        return findFile(agentDir, currentOsLibExt)
            ?: throw IllegalStateException("No $currentOsLibExt file found in the agent directory: ${agentDir.path}")
    }

    private fun getArgsMap(configuration: AgentConfiguration) = configuration.toAgentArguments()
        .filterValues { !it.isNullOrEmpty() }

}