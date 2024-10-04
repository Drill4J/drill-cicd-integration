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

import com.epam.drill.integration.common.agent.AgentCache
import com.epam.drill.integration.common.agent.Directory
import com.epam.drill.integration.common.agent.FileUrl
import java.io.File

class AgentCacheImpl(
    private val cacheDir: Directory
) : AgentCache {


    override fun clearAll() {
        cacheDir.deleteRecursively()
    }

    override fun clear(agentName: String, version: String, preset: String) {
        val file = File(cacheDir, getAgentFilename(agentName, preset, version))
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun get(
        agentName: String,
        version: String,
        preset: String,
        download: suspend (filename: String, downloadDir: Directory) -> Unit
    ): File {
        val file = File(cacheDir, getAgentFilename(agentName, preset, version))
        if (!file.exists()) {
            download(file.name, cacheDir)
        }
        return file
    }

    private fun getAgentFilename(agentName: String, preset: String, version: String) =
        "$agentName-$preset-$version.zip"
}