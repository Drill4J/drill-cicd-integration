package com.epam.drill.integration.common.git

class GitException(
    val command: String,
    val exitCode: Int,
    val outputMessage: String
) : RuntimeException(
    "Git command `$command` failed with error code $exitCode: $outputMessage"
)