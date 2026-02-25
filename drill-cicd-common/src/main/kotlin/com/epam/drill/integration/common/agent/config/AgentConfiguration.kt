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

    //transport settings
    var messageSendingMode: String? = null
    var messageQueueLimit: String? = null
    var messageMaxRetries: Int? = null
    var useProtobufSerializer: Boolean? = null
    var useGzipCompression: Boolean? = null

    var additionalParams: Map<String, String>? = null

    //coverage
    var coverageCollectionEnabled: Boolean? = null
    var envId: String? = null

    //class scanning
    var classScanningEnabled: Boolean? = null
    var scanClassPath: String? = null
    var enableScanClassLoaders: Boolean? = null
    var scanClassDelay: Int? = null

    //test tracing
    var testSessionId: String? = null
    var testTracingEnabled: Boolean? = null
    var testTracingPerTestSessionEnabled: Boolean? = null
    var testTracingPerTestLaunchEnabled: Boolean? = null

    //test prioritization
    var testTaskId: String? = null
    var recommendedTestsEnabled: Boolean? = null
    var recommendedTestsTargetAppId: String? = null
    var recommendedTestsTargetCommitSha: String? = null
    var recommendedTestsTargetBuildVersion: String? = null
    var recommendedTestsBaselineCommitSha: String? = null

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

        coverageCollectionEnabled?.let { isCoverageCollectionEnabled ->
            this[AgentConfiguration::coverageCollectionEnabled.name] = isCoverageCollectionEnabled.toString().lowercase()
        }

        classScanningEnabled?.let { isClassScanningEnabled ->
            this[AgentConfiguration::classScanningEnabled.name] = isClassScanningEnabled.toString().lowercase()
            if (isClassScanningEnabled) {
                scanClassPath?.let { this[AgentConfiguration::scanClassPath.name] = it }
                enableScanClassLoaders?.let { this[AgentConfiguration::enableScanClassLoaders.name] = enableScanClassLoaders.toString().lowercase() }
                if (enableScanClassLoaders == true) {
                    scanClassDelay?.let { this[AgentConfiguration::scanClassDelay.name] = it.toString() }
                }
            }
        }

        testTaskId?.let { this[AgentConfiguration::testTaskId.name] = it }
        testTracingEnabled?.let { isTestTracingEnabled ->
            this[AgentConfiguration::testTracingEnabled.name] = isTestTracingEnabled.toString().lowercase()
            if (isTestTracingEnabled) {
                testTracingPerTestSessionEnabled?.let {
                    this[AgentConfiguration::testTracingPerTestSessionEnabled.name] = it.toString().lowercase()
                }
                testTracingPerTestLaunchEnabled?.let {
                    this[AgentConfiguration::testTracingPerTestLaunchEnabled.name] = it.toString().lowercase()
                }
                testSessionId?.let { this[AgentConfiguration::testSessionId.name] = it }
            }
        }
        recommendedTestsEnabled?.let { isRecommendedTestsEnabled ->
            this[AgentConfiguration::recommendedTestsEnabled.name] =
                isRecommendedTestsEnabled.toString().lowercase()
            if (isRecommendedTestsEnabled) {
                recommendedTestsTargetAppId?.let { this[AgentConfiguration::recommendedTestsTargetAppId.name] = it }
                recommendedTestsTargetCommitSha?.let {
                    this[AgentConfiguration::recommendedTestsTargetCommitSha.name] = it
                }
                recommendedTestsTargetBuildVersion?.let {
                    this[AgentConfiguration::recommendedTestsTargetBuildVersion.name] = it
                }
                recommendedTestsBaselineCommitSha?.let {
                    this[AgentConfiguration::recommendedTestsBaselineCommitSha.name] = it
                }
            }
        }

        messageSendingMode?.let { this[AgentConfiguration::messageSendingMode.name] = it }
        messageQueueLimit?.let { this[AgentConfiguration::messageQueueLimit.name] = it }
        messageMaxRetries?.let { this[AgentConfiguration::messageMaxRetries.name] = it.toString() }
        useProtobufSerializer?.let { this[AgentConfiguration::useProtobufSerializer.name] = it.toString().lowercase() }
        useGzipCompression?.let { this[AgentConfiguration::useGzipCompression.name] = it.toString().lowercase() }

        additionalParams?.let { this.putAll(it) }
    }
}

enum class AgentMode {
    NATIVE,
    JAVA
}
