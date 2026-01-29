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

open class AgentConfiguration {
    var apiUrl: String? = null
    var apiKey: String? = null
    var groupId: String? = null
    var appId: String? = null
    var packagePrefixes: Array<String> = emptyArray()
    var buildVersion: String? = null
    var commitSha: String? = null

    var logLevel: String? = null
    var logFile: File? = null

    var version: String? = null
    var downloadUrl: String? = null
    var zipPath: File? = null

    var githubRepository: String = "Drill4J/java-agent"
    var agentName: String = "javaAgent"

    var agentMode: AgentMode = AgentMode.NATIVE

    var additionalParams: Map<String, String>? = null

    //coverage
    var envId: String? = null

    //class scanning
    var scanClassPath: String? = null
    var classScanningEnabled: Boolean? = null


    //test tracking
    var testTaskId: String? = null

    var recommendedTestsEnabled: Boolean? = null
    var recommendedTestsCoveragePeriodDays: Int? = null
    var recommendedTestsTargetAppId: String? = null
    var recommendedTestsTargetCommitSha: String? = null
    var recommendedTestsTargetBuildVersion: String? = null
    var recommendedTestsBaselineCommitSha: String? = null

    var testTracingEnabled: Boolean? = null
    var testLaunchMetadataSendingEnabled: Boolean? = null

    open fun toAgentArguments() = mutableMapOf<String, String?>().apply {
        this[AgentConfiguration::apiUrl.name] = apiUrl
        this[AgentConfiguration::apiKey.name] = apiKey
        this[AgentConfiguration::groupId.name] = groupId
        this[AgentConfiguration::logLevel.name] = logLevel
        this[AgentConfiguration::logFile.name] = logFile?.absolutePath

        this[AgentConfiguration::appId.name] = appId
        this[AgentConfiguration::packagePrefixes.name] = packagePrefixes.joinToString(";")
        this[AgentConfiguration::buildVersion.name] = buildVersion
        this[AgentConfiguration::commitSha.name] = commitSha
        this[AgentConfiguration::envId.name] = envId
        this[AgentConfiguration::scanClassPath.name] = scanClassPath
        this[AgentConfiguration::classScanningEnabled.name] = classScanningEnabled.toString()

        testTaskId?.let { this[AgentConfiguration::testTaskId.name] = it }
        recommendedTestsEnabled?.let { enabled ->
            this[AgentConfiguration::recommendedTestsEnabled.name] = enabled.toString().lowercase()
            recommendedTestsCoveragePeriodDays?.let { this[AgentConfiguration::recommendedTestsCoveragePeriodDays.name] = it.toString() }
            recommendedTestsCoveragePeriodDays?.let { this[AgentConfiguration::recommendedTestsCoveragePeriodDays.name] = it.toString() }
            recommendedTestsTargetAppId?.let { this[AgentConfiguration::recommendedTestsTargetAppId.name] = it }
            recommendedTestsTargetCommitSha?.let { this[AgentConfiguration::recommendedTestsTargetCommitSha.name] = it }
            recommendedTestsTargetBuildVersion?.let { this[AgentConfiguration::recommendedTestsTargetBuildVersion.name] = it }
            recommendedTestsBaselineCommitSha?.let { this[AgentConfiguration::recommendedTestsBaselineCommitSha.name] = it }
        }
        testTracingEnabled?.let { this[AgentConfiguration::testTracingEnabled.name] = it.toString().lowercase() }
        testLaunchMetadataSendingEnabled?.let { this[AgentConfiguration::testLaunchMetadataSendingEnabled.name] = it.toString().lowercase() }

        additionalParams?.let { this.putAll(it) }
    }
}

enum class AgentMode {
    NATIVE,
    JAVA
}
