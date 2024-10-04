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

import com.epam.drill.integration.common.agent.impl.AgentCacheImpl
import org.gradle.api.Task
import java.io.File

val drillAgentFilesDir = File(System.getProperty("user.home"), ".drill/agents")

fun Task.drillClearAgentFileCache(config: DrillPluginExtension) {
    doFirst {
        val agentCache = AgentCacheImpl(drillAgentFilesDir)
        agentCache.clearAll()
        logger.lifecycle("Agent file cache has been cleared")
    }
}