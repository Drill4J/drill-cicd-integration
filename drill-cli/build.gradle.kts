import java.net.URI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat

plugins {
    kotlin("jvm")
    id("com.github.hierynomus.license")
    application
}

group = "com.epam.drill.integration"
version = rootProject.version

val kotlinxCoroutinesVersion: String by parent!!.extra

repositories {
    mavenLocal()
    mavenCentral()
}

tasks {
    jar {
        archiveBaseName.set("app")
        archiveVersion.set("")
        manifest {
            attributes("Main-Class" to "com.epam.drill.integration.cli.CliAppKt")
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }
}
java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly((kotlin("stdlib-jdk8")))
    implementation("com.github.ajalt.clikt:clikt:3.5.4")
    implementation(project(":drill-common"))
    implementation(project(":drill-gitlab"))
    implementation(project(":drill-github"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")
}

val jarMainClassName = "com.epam.drill.integration.cli.CliAppKt"

application {
    mainClass.set(jarMainClassName)
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    val runtimeJar by registering(Jar::class) {
        group = "build"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveBaseName.set("${project.name}-runtime")
        manifest.attributes["Main-Class"] = jarMainClassName
        from(
            sourceSets.main.get().output,
            configurations.runtimeClasspath.get().resolve().map(::zipTree)
        )
    }
    assemble.get().dependsOn(runtimeJar)
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
