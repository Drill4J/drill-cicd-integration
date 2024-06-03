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
package com.epam.drill.integration.common.git

import java.io.BufferedReader

fun getGitBranch(): String {
    return executeGitCommand("git rev-parse --abbrev-ref HEAD").trim()
}

fun getGitCommitInfo(): GitCommitInfo {
    val commitDetails = executeGitCommand("git log -1 --pretty=format:%H%n%ad%n%an%n%B").split("\n")
    if (commitDetails.size < 4)
        throw IllegalStateException("Failed to get commit log details")
    return GitCommitInfo(
        sha = commitDetails[0],
        date = commitDetails[1],
        author = commitDetails[2],
        message = commitDetails.subList(3, commitDetails.size).joinToString("\n").trim(),
    )
}

data class GitCommitInfo(
    val sha: String,
    val date: String,
    val author: String,
    val message: String
)

fun executeGitCommand(command: String): String {
    val process = ProcessBuilder(*command.split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()

    return process.inputStream.bufferedReader().use(BufferedReader::readText).trim()
}