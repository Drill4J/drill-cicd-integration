import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    application
    kotlin("jvm")
    id("com.github.hierynomus.license")
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
        group = "build"
        archiveBaseName.set("app")
        archiveVersion.set("")
        manifest.attributes["Main-Class"] = jarMainClassName
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from(
            sourceSets.main.get().output,
            configurations.runtimeClasspath.get().resolve().map(::zipTree)
        )
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")
    implementation("com.github.ajalt.clikt:clikt:3.5.4")
    implementation(project(":drill-common"))
    implementation(project(":drill-gitlab"))
    implementation(project(":drill-github"))
    compileOnly((kotlin("stdlib-jdk8")))
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
