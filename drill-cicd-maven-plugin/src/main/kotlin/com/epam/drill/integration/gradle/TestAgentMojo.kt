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

import com.epam.drill.integration.common.agent.config.TestAgentConfiguration
import com.epam.drill.integration.common.util.required
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(
    name = "enableTestAgent",
    defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true
)
class TestAgentMojo : AbstractAgentMojo() {
    @Parameter(property = "testAgent", required = true)
    var testAgent: TestAgentMavenConfiguration? = null

    override fun getAgentConfig() = TestAgentConfiguration().apply {
        val mavenConfig = this@TestAgentMojo
        val testAgent = mavenConfig.testAgent.required("appAgent")

        setGeneralAgentProperties(testAgent, mavenConfig)
    }
}