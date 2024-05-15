import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm")
    id("com.github.hierynomus.license")
    application
}

group = "com.epam.drill.integration"
version = rootProject.version

val kotlinxCoroutinesVersion: String by parent!!.extra
val jarMainClassName = "com.epam.drill.integration.cli.CliAppKt"

repositories {
    mavenLocal()
    mavenCentral()
}

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
    jar {
        archiveBaseName.set("app")
        archiveVersion.set("")
        manifest {
            attributes("Main-Class" to jarMainClassName)
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
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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
