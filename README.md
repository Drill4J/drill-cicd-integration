# Drill4J CI/CD Integration 

## Overview

Tools for integration with CI/CD systems such as Gitlab and GitHub.

## Modules
- **drill-cicd-gradle-plugin**: Gradle plugin for CI/CD integration
- **drill-cicd-maven-plugin**: Maven plugin for CI/CD integration
- **drill-cicd-cli**: CLI Application for CI/CD integration
- **drill-cicd-gitlab**: Gitlab integration library
- **drill-cicd-github**: GitHub integration library
- **drill-cicd-common**: Common library

## Development

Build all modules:
```shell
./gradlew build
./gradlew publishToMavenLocal
```

To use locally built version in Gradle projects add `mavenLocal` repository to `settings.gradle` file of test project:
```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```
