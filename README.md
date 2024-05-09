# Drill CI/CD Integration 

## Overview

Tools for integration with CI/CD systems such as Gitlab and GitHub.

## Modules

- **common**: Common library
- **gitlab**: Gitlab integration services
- **github**: GitHub integration services
- **gradle-plugin**: Gradle plugin for CI/CD integration
- **cli**: CLI Application for CI/CD integration

## Build

Build all modules:
```shell
gradle build
```

Publish common libraries
```shell
cd common
gradle publishToMavenLocal
cd gitlab
gradle publishToMavenLocal
cd github
gradle publishToMavenLocal
```

Publish Gradle plugin:
```shell
cd ../gradle-plugin
gradle publish
```

## Usage

### Gitlab integration with Gradle plugin

Add Gradle plugin to your Gradle configuration:
```kotlin
plugins {
    id("com.epam.drill.integration.drill-gradle-plugin") version "0.0.1"
}

drillCiCd {
    version = "0.0.1"
    //Drill4J group ID
    groupId = "realworld"
    //Drill4J agent ID
    agentId = "realworld-backend"
    //Drill4J API url
    drillApiUrl = "http://localhost:8090/api"
    //Drill4J Api Key
    drillApiKey = "your-drill-api-key-here"
    //Source branch of MR
    sourceBranch = System.getenv("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME")
    //Target branch of MR
    targetBranch = System.getenv("CI_MERGE_REQUEST_TARGET_BRANCH_NAME")
    //Commit SHA that triggered the pipeline
    latestCommitSha = System.getenv("CI_COMMIT_SHA")

    gitlab {
        //Gitlab API url
        gitlabApiUrl = "https://gitlab.com/api"
        //Gitlab API Private Token
        gitlabPrivateToken = "your-gitlab-token-here"
        //Gitlab project ID
        projectId = System.getenv("CI_PROJECT_ID")
        //Gitlab merge request ID
        mergeRequestId = System.getenv("CI_MERGE_REQUEST_IID")
    }
    //Only for GitHub integration
    github {
        //GitHub API url, https://api.github.com by default
        githubApiUrl = "https://api.github.com"
        //GitHub API Token
        githubToken = "your-github-token-here"
        //GitHub repository full name
        githubRepository = System.getenv("CI_PROJECT_ID")
        //GitHub pull request number
        mergeRequestId = System.getenv("CI_MERGE_REQUEST_IID")
    }
}
```

Run the Gradle command:
```shell
./gradle drillGitlabMergeRequestReport
```

### GitHub integration with Gradle plugin

Add Gradle plugin to your Gradle configuration:

```kotlin
plugins {
    id("com.epam.drill.integration.drill-gradle-plugin") version "0.0.1"
}

drillCiCd {
    version = "0.0.1"
    //Drill4J group ID
    groupId = "realworld"
    //Drill4J agent ID
    agentId = "realworld-backend"
    //Drill4J API url
    drillApiUrl = "http://localhost:8090/api"
    //Drill4J Api Key
    drillApiKey = "your-drill-api-key-here"
    //Source branch of MR
    sourceBranch = System.getenv("GITHUB_HEAD_REF")
    //Target branch of MR
    targetBranch = System.getenv("GITHUB_BASE_REF")
    //Commit SHA that triggered the pipeline
    latestCommitSha = System.getenv("GITHUB_SHA")

    github {
        //GitHub API url, https://api.github.com by default
        githubApiUrl =  System.getenv("GITHUB_API_URL")
        //GitHub API Token
        githubToken = "your-github-token-here"
        //GitHub repository full name
        githubRepository = System.getenv("GITHUB_REPOSITORY")
        //GitHub pull request number
        pullRequestNumber = System.getenv("GITHUB_REF").let {
            Regex("""refs/pull/(\d+)/merge""").find(it)?.groupValues?.get(1)
        }
    }
}
```

Run the Gradle command:
```shell
./gradle drillGithubPullRequestReport
```