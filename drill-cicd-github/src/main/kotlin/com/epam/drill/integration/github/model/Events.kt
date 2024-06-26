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

import kotlinx.serialization.Serializable


@Serializable
data class GithubUser(val login: String)

@Serializable
data class GithubRepository(
    val name: String,
    val fullName: String,
    val owner: GithubUser
)

@Serializable
data class GithubPullRequestBranch(
    val sha: String,
    val ref: String
)

@Serializable
data class GithubPullRequest(
    val number: Int,
    val user: GithubUser,
    val head: GithubPullRequestBranch,
    val base: GithubPullRequestBranch,
)

@Serializable
data class GithubEvent(
    val pullRequest: GithubPullRequest?,
    val repository: GithubRepository
)