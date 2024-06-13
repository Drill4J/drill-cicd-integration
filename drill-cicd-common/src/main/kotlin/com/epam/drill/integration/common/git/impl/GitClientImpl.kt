package com.epam.drill.integration.common.git.impl

import com.epam.drill.integration.common.git.GitClient
import com.epam.drill.integration.common.git.GitCommitInfo
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStream

class GitClientImpl: GitClient {
    private val logger = KotlinLogging.logger { }

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
            throw IllegalStateException(
                "Git command `$command` failed " +
                        "with error code ${process.exitValue()}: ${process.errorStream.readText()}"
            )
        }
        return process.inputStream.readText()
    }

    private fun InputStream.readText() = bufferedReader().use(BufferedReader::readText).trim()
}

