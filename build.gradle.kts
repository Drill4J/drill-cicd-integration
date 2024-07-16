import org.ajoberstar.grgit.Grgit

plugins {
    `maven-publish`
    `kotlin-dsl`.apply(false)
    kotlin("jvm").apply(false)
    id("org.ajoberstar.grgit")
    id("com.github.hierynomus.license").apply(false)
    id("io.github.gradle-nexus.publish-plugin")
    id("io.freefair.maven-central.validate-poms") version "8.6"
}

group = "com.epam.drill.integration"

if(version == Project.DEFAULT_VERSION) {
    val fromEnv: () -> String? = {
        System.getenv("GITHUB_REF")?.let { Regex("refs/tags/v(.*)").matchEntire(it)?.groupValues?.get(1) }
    }
    val fromGit: () -> String? = {
        val gitdir: (Any) -> Boolean = { projectDir.resolve(".git").isDirectory }
        takeIf(gitdir)?.let {
            val gitrepo = Grgit.open { dir = projectDir }
            val gittag = gitrepo.describe {
                tags = true
                longDescr = true
                match = listOf("v[0-9]*.[0-9]*.[0-9]*")
            }
            gittag?.trim()?.removePrefix("v")?.replace(Regex("-[0-9]+-g[0-9a-f]+$"), "")?.takeIf(String::any)
        }
    }
    version = fromEnv() ?: fromGit() ?: version
}

val kotlinVersion: String by extra
val kotlinxCollectionsVersion: String by extra
val kotlinxCoroutinesVersion: String by extra
val kotlinxSerializationVersion: String by extra

repositories {
    mavenCentral()
}

buildscript {
    dependencies.classpath("org.apache.commons:commons-configuration2:2.9.0")
    dependencies.classpath("commons-beanutils:commons-beanutils:1.9.4")
}

if (version == Project.DEFAULT_VERSION) {
    val fromEnv: () -> String? = {
        System.getenv("GITHUB_REF")?.let { Regex("refs/tags/v(.*)").matchEntire(it)?.groupValues?.get(1) }
    }
    val fromGit: () -> String? = {
        val gitdir: (Any) -> Boolean = { projectDir.resolve(".git").isDirectory }
        takeIf(gitdir)?.let {
            val gitrepo = Grgit.open { dir = projectDir }
            val gittag = gitrepo.describe {
                tags = true
                longDescr = true
                match = listOf("v[0-9]*.[0-9]*.[0-9]*")
            }
            gittag?.trim()?.removePrefix("v")?.replace(Regex("-[0-9]+-g[0-9a-f]+$"), "")?.takeIf(String::any)
        }
    }
    version = fromEnv() ?: fromGit() ?: version
}

subprojects {
    val constraints = setOf(
        dependencies.constraints.create("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"),
        dependencies.constraints.create("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"),
        dependencies.constraints.create("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"),
        dependencies.constraints.create("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"),
        dependencies.constraints.create("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-collections-immutable:$kotlinxCollectionsVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-collections-immutable-jvm:$kotlinxCollectionsVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$kotlinxCoroutinesVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxCoroutinesVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion"),
        dependencies.constraints.create("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$kotlinxSerializationVersion"),
    )
    configurations.all {
        dependencyConstraints += constraints
    }
    afterEvaluate {
        extensions.findByType(PublishingExtension::class)?.publications?.withType<MavenPublication> {
            pom {
                url.set("https://github.com/Drill4J/${rootProject.name}")
                scm {
                    connection.set("scm:git:https://github.com/Drill4J/${rootProject.name}.git")
                    developerConnection.set("scm:git:git@github.com:Drill4J/${rootProject.name}.git")
                    url.set("https://github.com/Drill4J/${rootProject.name}")
                }
                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("Drill4J")
                        name.set("Drill4J")
                        email.set("drill4j@gmail.com")
                        url.set("https://drill4j.github.io/")
                    }
                }
            }
            extensions.findByType(SigningExtension::class)?.apply {
                val propertyOrEnv: (String, String) -> String? = { property, env ->
                    findProperty(property)?.toString() ?: System.getenv(env)
                }
                val signingKey = propertyOrEnv("gpgSigningKey", "GPG_SIGNING_KEY")
                val passphrase = propertyOrEnv("gpgPassphrase", "GPG_PASSPHRASE")
                if (signingKey != null && passphrase != null) {
                    useInMemoryPgpKeys(signingKey, passphrase)
                    sign(this@withType)
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            val propertyOrEnv: (String, String) -> String? = { property, env ->
                findProperty(property)?.toString() ?: System.getenv(env)
            }
            username.set(propertyOrEnv("ossrhUserName", "OSSRH_USERNAME"))
            password.set(propertyOrEnv("ossrhToken", "OSSRH_TOKEN"))
        }
    }
}
