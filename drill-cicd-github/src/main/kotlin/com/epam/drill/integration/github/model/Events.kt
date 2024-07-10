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
package com.epam.drill.integration.github.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GithubUser(@SerialName("login") val login: String)

@Serializable
data class GithubRepository(
    @SerialName("name")
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("owner")
    val owner: GithubUser
)

@Serializable
data class GithubPullRequestBranch(
    @SerialName("sha")
    val sha: String,
    @SerialName("ref")
    val ref: String
)

@Serializable
data class GithubPullRequest(
    @SerialName("number")
    val number: Int,
    @SerialName("user")
    val user: GithubUser,
    @SerialName("head")
    val head: GithubPullRequestBranch,
    @SerialName("base")
    val base: GithubPullRequestBranch,
)

@Serializable
data class GithubEvent(
    @SerialName("pull_request")
    val pullRequest: GithubPullRequest?,
    @SerialName("repository")
    val repository: GithubRepository
)