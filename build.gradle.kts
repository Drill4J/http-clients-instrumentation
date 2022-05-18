import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*
import java.net.*
import com.hierynomus.gradle.license.tasks.*

plugins {
    kotlin("multiplatform")
    id("com.epam.drill.gradle.plugin.kni")
    id("com.github.hierynomus.license")
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val scriptUrl: String by extra

apply(from = rootProject.uri("$scriptUrl/git-version.gradle.kts"))

repositories {
    mavenLocal()
    mavenCentral()
    apply(from = rootProject.uri("$scriptUrl/maven-repo.gradle.kts"))
}

val knasmVersion: String by extra
val javassistVersion: String by extra
val drillLogger: String by extra
val kniVersion: String by extra
val drillJvmApiLibVersion: String by extra

val nativeTargets = mutableSetOf<KotlinNativeTarget>()
val currentPlatformName = HostManager.host.presetName

kotlin {
    targets {
        nativeTargets.addAll(
            sequenceOf(
                mingwX64(),
                linuxX64(),
                macosX64()
            )
        )
        nativeTargets.forEach { target ->
            //TODO EPMDJ-8696 remove
            if (currentPlatformName == target.name) {
                target.compilations["main"].setCommonSources()
            }
        }

        jvm {
            compilations["main"].defaultSourceSet {
                dependencies {
                    //TODO Compile only ?
                    implementation("org.javassist:javassist:$javassistVersion")
                    implementation("com.epam.drill.knasm:knasm:$knasmVersion")
                    implementation("com.epam.drill.kni:runtime:$kniVersion")
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.epam.drill.knasm:knasm:$knasmVersion")
                implementation("com.epam.drill.logger:logger:$drillLogger") {
                    //TODO EPMDJ-8703 exclude in logger
                    exclude("org.slf4j")
                }
            }
        }
        //TODO EPMDJ-8696 Rename to commonNative
        val commonNativeDependenciesOnly by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("com.epam.drill:jvmapi:$drillJvmApiLibVersion")
                implementation("com.epam.drill.logger:logger:$drillLogger")
                implementation("com.epam.drill.knasm:knasm:$knasmVersion")
                implementation("com.epam.drill.kni:runtime:$kniVersion")
            }
        }

        val linuxX64Main by getting {
            dependsOn(commonNativeDependenciesOnly)
        }
        val mingwX64Main by getting {
            dependsOn(commonNativeDependenciesOnly)
        }
        val macosX64Main by getting {
            dependsOn(commonNativeDependenciesOnly)
        }


    }
    kni {
        jvmTargets = sequenceOf(jvm())
        additionalJavaClasses = sequenceOf()
        nativeCrossCompileTarget = nativeTargets.asSequence()
        excludedClasses = sequenceOf("com.epam.drill.logger.NativeApi")
    }
}

tasks {
    val generateNativeClasses by getting {}

    withType<ProcessResources> {
        dependsOn(generateNativeClasses)
    }
    //TODO EPMDJ-8696 remove copy
    val copy = nativeTargets.filter { it.name != currentPlatformName }.map {
        register<Copy>("copy for ${it.name}") {
            from(file("src/commonNative/kotlin"))
            into(file("src/${it.name}Main/kotlin/gen"))
        }
    }
    val copyCommon by registering(DefaultTask::class) {
        group = "build"
        copy.forEach { dependsOn(it) }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
        dependsOn(copyCommon)
        dependsOn(generateNativeClasses)
    }
    val cleanExtraData by registering(Delete::class) {
        group = "build"
        nativeTargets.forEach {
            val path = "src/${it.name}Main/kotlin/"
            delete(file("${path}kni"), file("${path}gen"))
        }
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

//TODO EPMDJ-8696 remove
fun KotlinNativeCompilation.setCommonSources(modulePath: String = "src/commonNative") {
    defaultSourceSet {
        kotlin.srcDir(file("${modulePath}/kotlin"))
        resources.setSrcDirs(listOf("${modulePath}/resources"))
    }
}

