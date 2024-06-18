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

For using locally in Gradle projects is necessary to add `mavenLocal` repository to your `settings.gradle` file:
```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

## Usage

### General settings

#### Gradle plugin

Add Drill4J CI/CD plugin to your Gradle build file:
```kotlin
plugins {
    id("com.epam.drill.integration.cicd") version "0.0.1"
}
```

Add general properties to your Gradle build file:

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
    //Other settings
    ...
}
```

#### Maven plugin

Add general properties to your Drill4J plugin configuration:

```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <!-- Drill4J group ID -->
        <groupId>realworld</groupId>
        <!-- Drill4J application ID -->
        <appId>realworld-backend</appId>
        <!-- Drill4J API url -->
        <drillApiUrl>http://localhost:8090/api</drillApiUrl>
        <!-- Drill4J Api Key -->
        <drillApiKey>your-drill-api-key-here</drillApiKey>
        
        <!-- Other settings -->
        ...
    </configuration>
    <executions>
        <execution>
            <goals>
                <!-- Executable goals -->
                ...
            </goals>
        </execution>
    </executions>
</plugin>
```

### Testing report in Gitlab Merge Requests

#### Gradle plugin

Add Gitlab integration properties to your Kotlin Gradle build file:

```kotlin
drillCiCd {
    //General properties
    ...
    
    gitlab {
        //Gitlab API url
        apiUrl = "https://gitlab.com/api"
        //Gitlab API Private Token
        privateToken = "your-gitlab-token-here"
    }
}
```

Run the Gradle command in your merge request pipeline after a test stage:
```shell
./gradlew drillGitlabMergeRequestReport
```

#### Maven plugin

Add Gitlab integration properties to your Maven build file:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <!-- General properties -->
        ...
        
        <gitlab>
            <!-- Gitlab API url -->
            <apiUrl>https://api.github.com</apiUrl>
            <!-- Gitlab API Private Token -->
            <privateToken>someToken</privateToken>
        </gitlab>
    </configuration>
    <executions>
       ...
    </executions>
</plugin>
```

Set up executable goals:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        ...
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

Run the Maven command in your merge request pipeline after a test stage:
```shell
mvn drill-maven-plugin:drillGitlabMergeRequestReport
```

### Testing report in GitHub Pull Requests

#### Gradle plugin

Add GitHub integration properties to your Kotlin Gradle build file:
```kotlin
drillCiCd {
    //General properties
    ...
    
    github {
        //GitHub API Token
        token = "your-github-token-here"
    }
}
```

Run the Gradle command in your pull request workflow after a test stage:
```shell
./gradlew drillGithubPullRequestReport
```
#### Maven plugin

Add GitHub properties to your Maven build file:

```xml

<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <!-- General properties -->
        ...
        
        <github>
            <!-- GitHub Token -->
            <token>your-github-token-here</token>
        </github>
    </configuration>
    <executions>
        ...
    </executions>
</plugin>
```

Set up executable goals:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        ...
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

Run the Maven command in your pull request workflow after a test stage:
```shell
mvn drill-maven-plugin:drillGithubPullRequestReport
```

### Sending build information

#### Gradle plugin

Add information about a current build version to your Gradle build file:
```kotlin
drillCiCd {
    //General properties
    ...
    //Version of current build (optional)
    buildVersion = "1.2.3"   
}
```

Run the Gradle command at the build stage of your CI/CD pipeline:
```shell
./gradlew drillSendBuildInfo
```

#### Maven plugin

Add information about a current build version to your Maven build file:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <!-- General properties -->
        ...
        
        <!-- Version of current build (optional) -->
        <buildVersion>1.2.3</buildVersion>
    </configuration>
    <executions>
        ...
    </executions>
</plugin>
```

Set up executable goals:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        ...
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>drillSendBuildInfo</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run the Maven command at the build stage of your CI/CD pipeline:
```shell
mvn drill-maven-plugin:drillSendBuildInfo
```


### Generating a Change Testing Report

#### Gradle plugin

Choose a strategy for finding baseline commits to compare.

Search by tags strategy:
```kotlin
drillCiCd {
    //General properties
    ...
    
    baseline {
        //Strategy used to find a baseline commit (optional, SEARCH_BY_TAG by default)
        searchStrategy = "SEARCH_BY_TAG"
        //Only consider tags matching the given pattern
        tagPattern = "v[0-9].[0-9].[0-9]*"
    }
}
```
Search by merge base strategy:
```kotlin
drillCiCd {
    //General properties
    ...
    
    baseline {
        //Strategy used to find a baseline commit (optional, SEARCH_BY_TAG by default)
        searchStrategy = "SEARCH_BY_MERGE_BASE"
        //A branch, tag, or commit of a baseline version to compare to the current build
        targetRef = "main"
    }
}
```

Run the Gradle command after running the tests:
```shell
./gradlew drillGenerateChangeTestingReport
```

Find a report file in `/build/reports/drill/` directory.

#### Maven plugin

Choose a strategy for finding baseline commits to compare.

Search by tags strategy:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <!-- General properties -->
        ...
        
        <baseline>
            <!-- Strategy used to find a baseline commit (optional, 'SEARCH_BY_TAG' by default) -->
            <searchStrategy>SEARCH_BY_TAG</searchStrategy>
            <!-- Only consider tags matching the given pattern (optional, '*' by default) -->
            <tagPattern>v[0-9].[0-9].[0-9]*</tagPattern>
        </baseline>
    </configuration>
    <executions>
        ...
    </executions>
</plugin>
```
Search by merge base strategy:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        <!-- General properties -->
        ...
        
        <baseline>
            <!-- Strategy used to find a baseline commit (optional, 'SEARCH_BY_TAG' by default) -->
            <searchStrategy>SEARCH_BY_MERGE_BASE</searchStrategy>
            <!-- A branch, tag, or commit of a baseline version to compare to the current build -->
            <targetRef>main</targetRef>
        </baseline>
    </configuration>
    <executions>
        ...
    </executions>
</plugin>
```

Set up executable goals:
```xml
<plugin>
    <groupId>com.epam.drill.integration</groupId>
    <artifactId>drill-maven-plugin</artifactId>
    <version>0.0.1</version>
    <configuration>
        ...
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>drillGenerateChangeTestingReport</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run the Maven command after running the tests:
```shell
mvn drill-maven-plugin:drillGenerateChangeTestingReport
```

Find a report file in `/target/reports/drill/` directory.