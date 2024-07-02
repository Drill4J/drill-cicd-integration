import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.konan.target.HostManager
import java.net.URI

@Suppress("RemoveRedundantBackticks")
plugins {
    `signing`
    `maven-publish`
    kotlin("jvm")
    id("com.github.hierynomus.license")
}

group = "com.epam.drill.integration"
version = "0.0.5-SNAPSHOT"

val kotlinVersion: String by parent!!.extra
val ktorVersion: String by parent!!.extra
val kotlinxCoroutinesVersion: String by parent!!.extra
val kotlinxSerializationVersion: String by parent!!.extra
val microutilsLoggingVersion: String by parent!!.extra

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.maven:maven-core:3.8.1")
    implementation("org.apache.maven:maven-plugin-api:3.8.1")
    implementation("org.apache.maven.plugin-tools:maven-plugin-annotations:3.6.1")
    implementation("org.apache.maven.plugins:maven-surefire-plugin:2.22.2")
    implementation("org.twdata.maven:mojo-executor:2.3.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")

    implementation(project(":drill-cicd-common"))
    implementation(project(":drill-cicd-github"))
    implementation(project(":drill-cicd-gitlab"))
}

@Suppress("UNUSED_VARIABLE")
tasks {
    val sourcesJar by registering(Jar::class) {
        from(sourceSets.main.get().allSource)
        from(project(":drill-cicd-common").sourceSets.main.get().allSource)
        from(project(":drill-cicd-github").sourceSets.main.get().allSource)
        from(project(":drill-cicd-gitlab").sourceSets.main.get().allSource)
        archiveClassifier.set("sources")
    }
    val javadocJar by registering(Jar::class) {
        dependsOn(project(":drill-cicd-common").tasks.javadoc)
        dependsOn(project(":drill-cicd-github").tasks.javadoc)
        dependsOn(project(":drill-cicd-gitlab").tasks.javadoc)

        from(javadoc.get().destinationDir)
        from(project(":drill-cicd-common").tasks.javadoc.get().destinationDir)
        from(project(":drill-cicd-github").tasks.javadoc.get().destinationDir)
        from(project(":drill-cicd-gitlab").tasks.javadoc.get().destinationDir)
        archiveClassifier.set("javadoc")
    }
    val mvnInstall by registering(Exec::class) {
        val args = if (HostManager.hostIsMingw) arrayOf("cmd", "/c", "mvnw.cmd") else arrayOf("sh", "./mvnw")
        commandLine(*args, "install", "-Ddrill.plugin.version=$version", "-Dkotlin.version=$kotlinVersion")
        workingDir(project.projectDir)
        standardOutput = System.out
        outputs.file("target/drill-maven-plugin-$version.jar")
    }
    assemble.get().dependsOn(sourcesJar)
    assemble.get().dependsOn(javadocJar)
    assemble.get().dependsOn(mvnInstall)
    clean {
        delete("target")
    }
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks["sourcesJar"])
        artifact(tasks["javadocJar"])
        artifact(tasks["mvnInstall"].outputs.files.singleFile).builtBy(tasks["mvnInstall"])
        pom {
            name.set("Maven plugin for CI/CD integration")
            description.set("Maven plugin for CI/CD integration")
            withXml {
                asNode().appendNode("dependencies").apply {
                    // Kotlin
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "org.jetbrains.kotlin")
                        appendNode("artifactId", "kotlin-stdlib")
                        appendNode("version", kotlinVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "org.jetbrains.kotlin")
                        appendNode("artifactId", "kotlin-reflect")
                        appendNode("version", kotlinVersion)
                    }
                    // Ktor
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-cio")
                        appendNode("version", ktorVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-cio-jvm")
                        appendNode("version", ktorVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-json-jvm")
                        appendNode("version", ktorVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-core-jvm")
                        appendNode("version", ktorVersion)
                    }
                    // Logging
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-logging-jvm")
                        appendNode("version", ktorVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.github.microutils")
                        appendNode("artifactId", "kotlin-logging-jvm")
                        appendNode("version", microutilsLoggingVersion)
                    }
                    // Serialization
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-serialization")
                        appendNode("version", ktorVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "io.ktor")
                        appendNode("artifactId", "ktor-client-serialization-jvm")
                        appendNode("version", ktorVersion)
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "org.jetbrains.kotlinx")
                        appendNode("artifactId", "kotlinx-serialization-core-jvm")
                        appendNode("version", "1.5.1")
                    }
                    this.appendNode("dependency").apply {
                        appendNode("groupId", "org.jetbrains.kotlinx")
                        appendNode("artifactId", "kotlinx-serialization-json-jvm")
                        appendNode("version", "1.5.1")
                    }
                }
            }
        }
    }
}

@Suppress("UNUSED_VARIABLE")
license {
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
    val licenseFormatSources by tasks.registering(LicenseFormat::class) {
        source = fileTree("$projectDir/src").also {
            include("**/*.kt", "**/*.java", "**/*.groovy")
        }
    }
    val licenseCheckSources by tasks.registering(LicenseCheck::class) {
        source = fileTree("$projectDir/src").also {
            include("**/*.kt", "**/*.java", "**/*.groovy")
        }
    }
    tasks["license"].dependsOn(licenseCheckSources)
    tasks["licenseFormat"].dependsOn(licenseFormatSources)
}