package com.epam.drill.integration.common.git

interface GitClient {
    fun getGitBranch(): String
    fun getGitCommitInfo(): GitCommitInfo
    fun getMergeBaseCommitSha(targetRef: String): String
    fun fetch(depth: Int? = null)
}

data class GitCommitInfo(
    val sha: String,
    val date: String,
    val author: String,
    val message: String
)