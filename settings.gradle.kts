rootProject.name = "drill-cicd-integration"

pluginManagement {
    val kotlinVersion: String by extra
    val grgitVersion: String by extra
    val licenseVersion: String by extra
    val nexusPublishPluginVersion: String by extra
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.ajoberstar.grgit") version grgitVersion
        id("com.github.hierynomus.license") version licenseVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPublishPluginVersion
    }
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":drill-cicd-common")
include(":drill-cicd-gitlab")
include(":drill-cicd-github")
include(":drill-cicd-gradle-plugin")
include(":drill-cicd-maven-plugin")
include(":drill-cicd-cli")
