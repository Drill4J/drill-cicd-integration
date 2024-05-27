# Drill4J CI/CD Integration 

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
./gradlew build
./gradlew publishToMavenLocal
```

## Usage

### Gitlab integration with Gradle plugin

Add Gradle plugin to your Gradle configuration:
```kotlin
plugins {
    id("com.epam.drill.integration.drill-gradle-plugin") version "0.0.1"
}
```

Add Drill4J Gitlab integration properties to your Gradle configuration:

```kotlin
drillCiCd {
    //Drill4J group ID
    groupId = "realworld"
    //Drill4J application ID
    appId = "realworld-backend"
    //Drill4J API url
    drillApiUrl = "http://localhost:8090/api"
    //Drill4J Api Key
    drillApiKey = "your-drill-api-key-here"
    //Source branch of MR
    sourceBranch = System.getenv("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME")
    //Target branch of MR
    targetBranch = System.getenv("CI_MERGE_REQUEST_TARGET_BRANCH_NAME")
    //Commit SHA that triggered the pipeline
    commitSha = System.getenv("CI_COMMIT_SHA")

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
}
```

Run the Gradle command in the Merge Request Pipeline after a test stage:
```shell
./gradlew drillGitlabMergeRequestReport
```

### GitHub integration with Gradle plugin

Add Gradle plugin to your Gradle configuration:

```kotlin
plugins {
    id("com.epam.drill.integration.drill-gradle-plugin") version "0.0.1"
}
```
Add Drill4J GitHub integration properties to your Gradle configuration:
```kotlin
drillCiCd {
    //Drill4J group ID
    groupId = "realworld"
    //Drill4J application ID
    appId = "realworld-backend"
    //Drill4J API url
    drillApiUrl = "http://localhost:8090/api"
    //Drill4J Api Key
    drillApiKey = "your-drill-api-key-here"
    //Source branch of MR
    sourceBranch = System.getenv("GITHUB_HEAD_REF")
    //Target branch of MR
    targetBranch = System.getenv("GITHUB_BASE_REF")
    //Commit SHA that triggered the pipeline
    commitSha = System.getenv("GITHUB_SHA")

    github {
        //GitHub API url, "https://api.github.com" by default
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

Run the Gradle command in the Pull Request Pipeline after a test stage:
```shell
./gradlew drillGithubPullRequestReport
```

### Build stage integration

Add Gradle plugin to your Gradle configuration:

```kotlin
plugins {
    id("com.epam.drill.integration.drill-gradle-plugin") version "0.0.1"
}
```
Add Drill4J CI/CD integration properties to your Gradle configuration:
```kotlin
drillCiCd {
    //Drill4J group ID
    groupId = "realworld"
    //Drill4J application ID
    appId = "realworld-backend"
    //Drill4J API url
    drillApiUrl = "http://localhost:8090/api"
    //Drill4J Api Key
    drillApiKey = "your-drill-api-key-here"
    //Version of this build (optional)
    buildVersion = "1.2.3-rc1"   
}
```

Run the Gradle command at the build stage of your build pipeline:
```shell
./gradlew drillSendBuildInfo
```


### Gitlab integration with Maven plugin

Add Drill4J Gitlab integration to your Maven configuration:

```xml

<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <groupId>some-group-id</groupId>
        <agentId>some-agent-id</agentId>
        <sourceBranch>source</sourceBranch>
        <targetBranch>target</targetBranch>
        <latestCommitSha>foo</latestCommitSha>
        <drillApiUrl>http://example.com/api</drillApiUrl>
        <drillApiKey>secret-key</drillApiKey>
        <gitlab>
            <projectId>test</projectId>
            <gitlabApiUrl>https://api.gitlab.com</gitlabApiUrl>
            <gitlabPrivateToken>someToken</gitlabPrivateToken>
            <mergeRequestId>1</mergeRequestId>
        </gitlab>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>drillGitlabMergeRequestReport</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run the Maven command after a test stage:

```shell
./mvnw clean install
```

### GitHub integration with Maven plugin

Add Drill4J GitHub integration to your Maven configuration:

```xml

<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <groupId>some-group-id</groupId>
        <agentId>some-agent-id</agentId>
        <sourceBranch>source</sourceBranch>
        <targetBranch>target</targetBranch>
        <latestCommitSha>foo</latestCommitSha>
        <drillApiUrl>http://example.com/api</drillApiUrl>
        <drillApiKey>secret-key</drillApiKey>
        <github>
            <githubRepository>test</githubRepository>
            <githubApiUrl>https://api.github.com</githubApiUrl>
            <githubToken>someToken</githubToken>
            <pullRequestId>1</pullRequestId>
        </github>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>drillGithubPullRequestReport</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run the Maven command after a test stage:

```shell
./mvnw clean install
```