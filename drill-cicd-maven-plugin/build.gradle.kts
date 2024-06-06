import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.presetName
import java.lang.System
import java.net.URI

plugins {
    `maven-publish`
    id("com.github.hierynomus.license")
}

group = "com.epam.drill.integration"
version = rootProject.version

val kotlinVersion: String by extra

repositories {
    mavenLocal()
    mavenCentral()
}

@Suppress("UNUSED_VARIABLE")
tasks {
    val clean by registering(Exec::class) {
        val windows = HostManager.host.presetName == "mingwX64"
        val args = if (windows) arrayOf("cmd", "/c", "mvnw.cmd") else arrayOf("sh", "./mvnw")
        group = "build"
        standardOutput = System.out
        workingDir(projectDir)
        commandLine(*args, "clean", "-Ddrill.plugin.version=$version", "-Dkotlin.version=$kotlinVersion")
    }
    val install by registering(Exec::class) {
        val windows = HostManager.host.presetName == "mingwX64"
        val args = if (windows) arrayOf("cmd", "/c", "mvnw.cmd") else arrayOf("sh", "./mvnw")
        group = "build"
        standardOutput = System.out
        workingDir(projectDir)
        commandLine(*args, "install", "-Ddrill.plugin.version=$version", "-Dkotlin.version=$kotlinVersion")
    }
    publish.get().dependsOn(install)
    publishToMavenLocal.get().dependsOn(install)
}

publishing {
    publications.create<MavenPublication>("drill-cicd-maven-plugin") {
        artifact(tasks["sourcesJar"])
        artifact(tasks["javadocJar"])
        artifact(tasks["mvnInstall"].outputs.files.singleFile).builtBy(tasks["mvnInstall"])
        pom {
            name.set("Maven plugin for CI/CD integration")
            description.set("Maven plugin for CI/CD integration")
            withXml {
                asNode().appendNode("dependencies").appendNode("dependency").apply {
                    appendNode("groupId", "org.jetbrains.kotlin")
                    appendNode("artifactId", "kotlin-stdlib")
                    appendNode("version", kotlinVersion)
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