# Drill4J CI/CD Integration

## Overview

Tools for integration with CI/CD systems such as Gitlab and GitHub.

## Modules

- **drill-cicd-common**: Common library
- **drill-cicd-gitlab**: Gitlab integration services
- **drill-cicd-github**: GitHub integration services
- **drill-gradle-plugin**: Gradle plugin for CI/CD integration
- **drill-maven-plugin**: Maven plugin for CI/CD integration
- **drill-cli**: CLI Application for CI/CD integration

## Build

Build all modules:

```shell
./gradlew build
./gradlew publishToMavenLocal
```

For using locally built versions of CI/CD plugins in Gradle projects
is necessary to add `mavenLocal` repository to your `settings.gradle` file:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

## Usage

### Gitlab integration with Gradle plugin

Add Gradle plugin to your Gradle configuration:

```kotlin
plugins {
    id("com.epam.drill.integration.cicd") version "0.0.1"
}
```

Add Drill4J Gitlab integration properties to your Kotlin Gradle configuration:

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

    gitlab {
        //Gitlab API url
        apiUrl = "https://gitlab.com/api"
        //Gitlab API Private Token
        privateToken = "your-gitlab-token-here"
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
    id("com.epam.drill.integration.cicd") version "0.0.1"
}
```

Add Drill4J GitHub integration properties to your Kotlin Gradle configuration:

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

    github {
        //GitHub API Token
        token = "your-github-token-here"
    }
}
```

Run the Gradle command in the Pull Request Pipeline after a test stage:

```shell
./gradlew drillGithubPullRequestReport
```

### Build stage integration with Gradle plugin

Add Gradle plugin to your Gradle configuration:

```kotlin
plugins {
    id("com.epam.drill.integration.cicd") version "0.0.1"
}
```

Add Drill4J CI/CD integration properties to your Kotlin Gradle configuration:

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
    buildVersion = "1.2.3-rc.1"
}
```

Run the Gradle command at the build stage of your build pipeline:

```shell
./gradlew drillSendBuildInfo
```

### Gitlab integration with Maven plugin

Add Drill4J Gitlab integration to your Maven configuration:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.epam.drill.integration</groupId>
            <artifactId>drill-cicd-maven-plugin</artifactId>
            <version>0.0.1-beta.6</version>
            <configuration>
                <groupId>some-group-id</groupId>
                <appId>some-agent-id</appId>
                <drillApiUrl>http://example.com/api</drillApiUrl>
                <drillApiKey>drillApiKey</drillApiKey>
                <gitlab>
                    <projectId>123</projectId>
                    <commitSha>asd</commitSha>
                    <apiUrl>https://gitlab.com/api</apiUrl>
                    <privateToken>someToken</privateToken>
                </gitlab>
            </configuration>
        </plugin>
    </plugins>
</build>
```

And configure profile to run as command:

```xml

<profiles>
    <profile>
        <id>drillGitlabMergeRequestReport</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>com.epam.drill.integration</groupId>
                    <artifactId>drill-cicd-maven-plugin</artifactId>
                    <version>0.0.1-beta.6</version>
                    <executions>
                        <execution>
                            <id>drillGitlabMergeRequestReport</id>
                            <goals>
                                <goal>drillGitlabMergeRequestReport</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Run the Maven command after a test stage:

```shell
./mvnw clean install -PdrillGitlabMergeRequestReport
```

### GitHub integration with Maven plugin

Add Drill4J GitHub integration to your Maven configuration:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.epam.drill.integration</groupId>
            <artifactId>drill-cicd-maven-plugin</artifactId>
            <version>0.0.1-beta.6</version>
            <configuration>
                <groupId>some-group-id</groupId>
                <appId>some-agent-id</appId>
                <drillApiUrl>http://example.com/api</drillApiUrl>
                <drillApiKey>drillApiKey</drillApiKey>
                <github>
                    <token>someToken</token>
                    <privateToken>someToken</privateToken>
                </github>
            </configuration>
        </plugin>
    </plugins>
</build>
```

And configure profile to run as command:

```xml

<profiles>
    <profile>
        <id>drillGithubPullRequestReport</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>com.epam.drill.integration</groupId>
                    <artifactId>drill-cicd-maven-plugin</artifactId>
                    <version>0.0.1-beta.6</version>
                    <executions>
                        <execution>
                            <id>drillGithubPullRequestReport</id>
                            <goals>
                                <goal>drillGithubPullRequestReport</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Run the Maven command after a test stage:

```shell
./mvnw clean install -PdrillGithubPullRequestReport
```

### Build stage integration with Maven plugin

Add Maven plugin to your Maven configuration:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.epam.drill.integration</groupId>
            <artifactId>drill-cicd-maven-plugin</artifactId>
            <version>0.0.1-beta.6</version>
            <configuration>
                <groupId>some-group-id</groupId>
                <appId>some-agent-id</appId>
                <drillApiUrl>http://example.com/api</drillApiUrl>
                <drillApiKey>drillApiKey</drillApiKey>
                <buildVersion>1.2.3-rc.1</buildVersion>
            </configuration>
        </plugin>
    </plugins>
</build>
```

And configure profile to run as command:

```xml

<profiles>
    <profile>
        <id>drillSendBuildInfo</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>com.epam.drill.integration</groupId>
                    <artifactId>drill-cicd-maven-plugin</artifactId>
                    <version>0.0.1-beta.6</version>
                    <executions>
                        <execution>
                            <id>drillSendBuildInfo</id>
                            <goals>
                                <goal>drillSendBuildInfo</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Run the Gradle command at the build stage of your build pipeline:

```shell
./mvnw clean install -PdrillSendBuildInfo

```
