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
package com.epam.drill.integration.common.git.impl

import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.GitCommitInfo
import com.epam.drill.integration.common.git.GitException
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStream

class GitClientImpl : GitClient {
    private val logger = KotlinLogging.logger { }

    override fun getCurrentCommitSha(): String {
        return executeGitCommand("git rev-parse HEAD")
    }

    override fun describe(
        all: Boolean,
        tags: Boolean,
        abbrev: Int,
        matchPattern: String?,
        excludePattern: String?
    ): String {
        return executeGitCommand(
            "git describe" +
                    if (all) " --all" else "" +
                            if (tags) " --tags" else "" +
                                    " --abbrev=$abbrev"
        )
    }

    override fun revList(ref: String, n: Int): List<String> {
        return executeGitCommand("git rev-list -n $n $ref").split("\n")
    }

    override fun getGitBranch(): String {
        return executeGitCommand("git rev-parse --abbrev-ref HEAD")
    }

    override fun getGitCommitInfo(): GitCommitInfo {
        val commitDetails = executeGitCommand("git log -1 --pretty=format:%H%n%ad%n%an%n%B")
            .split("\n")
        if (commitDetails.size < 4)
            throw IllegalStateException("Failed to get commit log details")
        return GitCommitInfo(
            sha = commitDetails[0],
            date = commitDetails[1],
            author = commitDetails[2],
            message = commitDetails.subList(3, commitDetails.size).joinToString("\n"),
        )
    }

    override fun getMergeBaseCommitSha(targetRef: String): String {
        return executeGitCommand("git merge-base HEAD $targetRef")
    }

    override fun fetch(depth: Int?) {
        val depthParam = " --depth=$depth"
            .takeIf { depth != null && depth > 0 }
            ?: ""
        executeGitCommand("git fetch$depthParam")
    }

    private fun executeGitCommand(command: String): String {
        logger.info { "Executing git command: $command" }
        val process = ProcessBuilder(*command.split(" ").toTypedArray()).start()
        if (process.waitFor() != 0) {
            throw GitException(command, process.exitValue(), process.errorStream.readText())
        }
        return process.inputStream.readText()
    }

    private fun InputStream.readText() = bufferedReader().use(BufferedReader::readText).trim()
}

