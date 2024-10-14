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
package com.epam.drill.integration.common.agent

import java.io.File

interface AgentInstaller {
    suspend fun getDownloadUrl(githubRepository: String, version: String, osPreset: String): FileUrl?
    suspend fun download(downloadUrl: FileUrl, downloadDir: Directory): File
    fun unzip(zipFile: File, destinationDir: Directory): Directory
    fun findAgentFile(unzippedDir: Directory, fileExtension: String): File?
}

data class FileUrl(val url: String, val filename: String)
typealias Directory = File