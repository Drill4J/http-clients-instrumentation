import com.hierynomus.gradle.license.tasks.*
import java.net.*

plugins {
    kotlin("jvm")
    id("com.github.hierynomus.license")
    `maven-publish`
}

val ttlVersion: String by extra
val atomicfuVersion: String by extra
val drillLoggerVersion: String by extra
val scriptUrl: String by extra



apply(from = rootProject.uri("$scriptUrl/git-version.gradle.kts"))

repositories {
    mavenLocal()
    mavenCentral()
    apply(from = "$scriptUrl/maven-repo.gradle.kts")
}



dependencies {
    implementation("com.alibaba:transmittable-thread-local:$ttlVersion")
    implementation("com.epam.drill.logger:logger:$drillLoggerVersion")
    implementation("org.jetbrains.kotlinx:atomicfu:$atomicfuVersion")

}

val licenseFormatSettings by tasks.registering(LicenseFormat::class) {
    source = fileTree(project.projectDir).also {
        include("**/*.kt", "**/*.java", "**/*.groovy")
        exclude("**/.idea")
    }.asFileTree
}

license {
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
}

tasks["licenseFormat"].dependsOn(licenseFormatSettings)


val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("publish-instrumentation") {
            artifact(tasks.jar.get())
            artifactId = rootProject.name
            artifact(sourcesJar.get())
        }
    }
}
