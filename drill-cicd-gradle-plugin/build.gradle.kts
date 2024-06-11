import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    signing
    `maven-publish`
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.github.hierynomus.license")
}

group = "com.epam.drill.integration"
version = rootProject.version

val kotlinxCoroutinesVersion: String by parent!!.extra

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    plugins {
        create("cicd") {
            id = "${group}.cicd"
            implementationClass = "com.epam.drill.integration.gradle.DrillCiCdIntegrationGradlePlugin"
            displayName = "Gradle plugin for CI/CD integration"
            description = "Gradle plugin for CI/CD integration"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    compileOnly((kotlin("stdlib-jdk8")))
    compileOnly((kotlin("gradle-plugin")))
    implementation(project(":drill-cicd-common"))
    implementation(project(":drill-cicd-gitlab"))
    implementation(project(":drill-cicd-github"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("Gradle plugin for CI/CD integration")
            description.set("Gradle plugin for CI/CD integration")
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
}
