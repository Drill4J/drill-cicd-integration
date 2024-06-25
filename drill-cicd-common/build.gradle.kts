import com.hierynomus.gradle.license.tasks.LicenseCheck
import com.hierynomus.gradle.license.tasks.LicenseFormat
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    `signing`
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.hierynomus.license")
}

group = "com.epam.drill.integration"
version = rootProject.version

val kotlinxSerializationVersion: String by extra
val ktorVersion: String by parent!!.extra
val microutilsLoggingVersion: String by parent!!.extra
val kotlinVersion: String by parent!!.extra
val mockitoKotlinVersion: String by parent!!.extra
val logbackClassicVersion: String by parent!!.extra

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

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$microutilsLoggingVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        // Enable all test events to be logged
        testLogging {
            // Show the standard output and error for every test
            showStandardStreams = true
            // Show the stack trace for every test failure
            exceptionFormat = TestExceptionFormat.FULL
            // Log test events
            events("passed", "skipped", "failed")
            // Customize log level for debug
            debug {
                events("started", "skipped", "passed", "failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
            // Customize log level for info
            info {
                events("started", "skipped", "passed", "failed")
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}


publishing {
    publications.create<MavenPublication>("drill-cicd-common") {
        from(components["java"])
        pom {
            name.set("Drill CICD integration common library")
            description.set("Drill CICD integration common library")
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
