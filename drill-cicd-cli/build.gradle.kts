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
val logbackClassicVersion: String by parent!!.extra

repositories {
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
    val runtimeJar by registering(Jar::class) {
        manifest.attributes["Main-Class"] = jarMainClassName
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from(
            sourceSets.main.get().output,
            configurations.runtimeClasspath.get().resolve().map(::zipTree)
        )
        archiveFileName.set("drill-cli-${project.version}.jar")
    }
    assemble.get().dependsOn(runtimeJar)
}


dependencies {
    compileOnly((kotlin("stdlib-jdk8")))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("com.github.ajalt.clikt:clikt:3.3.0")
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    implementation(project(":drill-cicd-common"))
    implementation(project(":drill-cicd-gitlab"))
    implementation(project(":drill-cicd-github"))
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
