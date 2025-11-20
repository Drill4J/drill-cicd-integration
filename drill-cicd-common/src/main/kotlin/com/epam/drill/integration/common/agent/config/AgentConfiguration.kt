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
package com.epam.drill.integration.common.agent.config

import java.io.File

abstract class AgentConfiguration {
    var apiUrl: String? = null
    var apiKey: String? = null
    var groupId: String? = null

    var logLevel: String? = null
    var logFile: File? = null

    var version: String? = null
    var downloadUrl: String? = null
    var zipPath: File? = null

    var additionalParams: Map<String, String>? = null

    var agentMode: AgentMode = AgentMode.NATIVE

    abstract val githubRepository: String
    abstract val agentName: String

    open fun toAgentArguments() = mutableMapOf<String, String?>().apply {
        this[AgentConfiguration::apiUrl.name] = apiUrl
        this[AgentConfiguration::apiKey.name] = apiKey
        this[AgentConfiguration::groupId.name] = groupId
        this[AgentConfiguration::logLevel.name] = logLevel
        this[AgentConfiguration::logFile.name] = logFile?.absolutePath
        additionalParams?.let { this.putAll(it) }
    }
}

enum class AgentMode {
    NATIVE,
    JAVA
}
