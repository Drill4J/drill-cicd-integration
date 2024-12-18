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

class AppAgentConfiguration : AgentConfiguration() {
    var appId: String? = null
    var packagePrefixes: Array<String> = emptyArray()
    var buildVersion: String? = null
    var commitSha: String? = null
    var envId: String? = null

    override val githubRepository: String = "Drill4J/java-agent"
    override val agentName: String = "appAgent"

    override fun toAgentArguments(): MutableMap<String, String?> {
        return super.toAgentArguments().apply {
            this[AppAgentConfiguration::appId.name] = appId
            this[AppAgentConfiguration::packagePrefixes.name] = packagePrefixes.joinToString(";")
            this[AppAgentConfiguration::buildVersion.name] = buildVersion
            this[AppAgentConfiguration::commitSha.name] = commitSha
            this[AppAgentConfiguration::envId.name] = envId
        }
    }

}
