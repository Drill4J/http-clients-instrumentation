import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import java.net.*
import com.hierynomus.gradle.license.tasks.*

plugins {
    kotlin("multiplatform")
    id("com.epam.drill.gradle.plugin.kni")
    id("com.github.hierynomus.license")
    id("com.epam.drill.cross-compilation")
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://drill4j.jfrog.io/artifactory/drill")
    maven(url = "https://oss.jfrog.org/oss-release-local")
}

val knasmVersion: String by extra
val javassistVersion: String by extra
val drillLogger: String by extra
val kniVersion: String by extra
val drillJvmApiLibVersion: String by extra

val nativeTargets = mutableSetOf<KotlinNativeTarget>()

val kniOutputDir = "src/kni/kotlin"

kotlin {
    mingwX64()
    linuxX64()
    macosX64()

    sourceSets {
        commonMain {
            dependencies {
                implementation("com.epam.drill.knasm:knasm:$knasmVersion")
            }
        }
    }
    crossCompilation {
        common {
            defaultSourceSet {
                dependsOn(sourceSets.named("commonMain").get())
                dependencies {
                    implementation("com.epam.drill:jvmapi:$drillJvmApiLibVersion")
                    implementation("com.epam.drill.logger:logger:$drillLogger")
                    implementation("com.epam.drill.knasm:knasm:$knasmVersion")
                    implementation("com.epam.drill.kni:runtime:$kniVersion")
                    implementation("com.epam.drill:jvmapi:$drillJvmApiLibVersion")
                }
            }
        }
    }

    kni {
        jvmTargets = sequenceOf(jvm())
        additionalJavaClasses = sequenceOf()
        srcDir = kniOutputDir
    }

    jvm {
        compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.javassist:javassist:$javassistVersion")
                implementation("com.epam.drill.knasm:knasm:$knasmVersion")
                implementation("com.epam.drill.kni:runtime:$kniVersion")
                implementation("com.epam.drill.logger:logger:$drillLogger")
            }
        }
    }
}

tasks {
    val generateNativeClasses by getting {}
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
        dependsOn(generateNativeClasses)
    }
    val cleanExtraData by registering(Delete::class) {
        group = "build"
        delete(kniOutputDir)
    }

    clean {
        dependsOn(cleanExtraData)
    }
}

val licenseFormatSettings by tasks.registering(LicenseFormat::class) {
    source = fileTree(project.projectDir).also {
        include("**/*.kt", "**/*.java", "**/*.groovy")
        exclude("**/.idea")
    }.asFileTree
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
}

tasks["licenseFormat"].dependsOn(licenseFormatSettings)


