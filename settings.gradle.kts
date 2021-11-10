rootProject.name = "http-clients-instrumentation"

val scriptUrl: String by extra
apply(from = "$scriptUrl/maven-repo.settings.gradle.kts")

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://drill4j.jfrog.io/artifactory/drill")
        gradlePluginPortal()
    }
    val kotlinVersion: String by extra
    val kniVersion: String by extra
    val licenseVersion: String by extra
    val drillGradlePluginVersion: String by extra
    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("com.epam.drill.gradle.plugin.kni") version kniVersion
        id("com.github.hierynomus.license") version licenseVersion
        id("com.epam.drill.cross-compilation") version drillGradlePluginVersion
    }
}
