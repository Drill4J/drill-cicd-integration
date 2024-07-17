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

class TestAgentConfiguration : AgentConfiguration() {
    var testTaskId: String? = null
    var labels: Map<String, String>? = null

    override val githubRepository: String = "Drill4J/autotest-agent"
    override val agentName: String = "testAgent"


    override fun toAgentArguments(): MutableMap<String, String?> {
        return super.toAgentArguments().apply {
            testTaskId?.let { this[TestAgentConfiguration::testTaskId.name] = it }
            labels?.let {
                this[TestAgentConfiguration::labels.name] = it.map { (k, v) -> "$k:$v" }.joinToString(";")
            }
        }
    }

}
