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

import com.epam.drill.integration.common.agent.config.AppAgentConfiguration
import com.epam.drill.integration.common.git.impl.GitClientImpl
import com.epam.drill.integration.common.util.required
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(
    name = "enableAppAgent",
    defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true
)
class AppAgentMojo : AbstractAgentMojo() {
    @Parameter(property = "appAgent", required = true)
    var appAgent: AppAgentMavenConfiguration? = null

    @Parameter(property = "appId", required = true)
    var appId: String? = null

    @Parameter(property = "packagePrefixes", required = true)
    var packagePrefixes: String? = null

    @Parameter(property = "buildVersion", required = false)
    var buildVersion: String? = null

    private val gitClient = GitClientImpl()

    override fun getAgentConfig() = AppAgentConfiguration().apply {
        val mavenConfig = this@AppAgentMojo
        val appAgent = mavenConfig.appAgent.required("appAgent")

        setGeneralAgentProperties(appAgent, mavenConfig)
        appId = mavenConfig.appId.required("appId")
        packagePrefixes = mavenConfig.packagePrefixes.required("packagePrefixes")
            .split(*arrayOf(",", ";"))
            .map(String::trim)
            .toTypedArray()
        additionalParams = mapOf(
            "scanClassPath" to "target/classes;target/test-classes"
        ) + (additionalParams ?: emptyMap())
        buildVersion = mavenConfig.buildVersion
        commitSha = runCatching {
            gitClient.getCurrentCommitSha()
        }.onFailure {
            log.warn("Unable to retrieve the current commit SHA. The 'commitSha' parameter will not be set. Error: ${it.message}")
        }.getOrNull()
    }
}