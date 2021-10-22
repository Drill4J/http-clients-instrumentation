import com.hierynomus.gradle.license.tasks.*
import java.net.*

plugins {
    kotlin("jvm")
    id("com.github.hierynomus.license")
}

repositories {
    mavenLocal()
    mavenCentral()
}

val licenseFormatSettings by tasks.registering(LicenseFormat::class) {
    source = fileTree(project.projectDir).also {
        include("**/*.kt", "**/*.java", "**/*.groovy")
        exclude("**/.idea")
    }.asFileTree
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
}

tasks["licenseFormat"].dependsOn(licenseFormatSettings)


