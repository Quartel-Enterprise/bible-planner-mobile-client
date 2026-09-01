import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.compose.hotReload) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

val ktlintVersion: String = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")
    .findVersion("ktlint")
    .get()
    .requiredVersion

// version.xcconfig is the single source of truth for the app version: iOS reads it through
// `#include` in Config.xcconfig, and the lines below expose the same values to every Gradle
// module as `versionName`/`versionCode` project properties.
val appVersion: Map<String, String> = providers
    .fileContents(layout.projectDirectory.file("version.xcconfig"))
    .asText
    .get()
    .lineSequence()
    .filter { line -> '=' in line && !line.trimStart().startsWith("//") }
    .associate { line ->
        val (key, value) = line.split('=', limit = 2)
        key.trim() to value.trim()
    }

allprojects {
    appVersion.forEach { (key, value) -> extra[key] = value }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    if (path != ":tools:ktlint-custom-rules") {
        dependencies {
            add("ktlintRuleset", project(":tools:ktlint-custom-rules"))
        }
    }

    extensions.configure<KtlintExtension> {
        version.set(ktlintVersion)
        debug.set(false)
        verbose.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)

        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            include("**/kotlin/**")
        }

        reporters {
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    // Configure Kotlin compiler to use -Xexpect-actual-classes flag
    // This suppresses warnings about expect/actual classes being in Beta
    // This applies to all Kotlin compilation, including when ktlint uses the compiler
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

// Store listing assets are versioned under store_listings/, which is why they are collected out of
// the generators' Fastlane-shaped output into one folder per platform and language. Screens are
// produced by the `testAndroidHostTest` task of whichever feature module owns the screen.
private val storeScreenshotLocales = mapOf(
    "pt-BR" to "pt_br",
    "en-US" to "en",
    "es" to "es",
)

private val storeScreenshotDevices = mapOf(
    "images/phoneScreenshots" to ("android" to "phone"),
    "images/sevenInchScreenshots" to ("android" to "tablet_7"),
    "images/tenInchScreenshots" to ("android" to "tablet_10"),
    "iphone65" to ("ios" to "iphone_6_5"),
    "iphone67" to ("ios" to "iphone_6_7"),
    "ipad13" to ("ios" to "ipad_13"),
    "ipad11" to ("ios" to "ipad_11"),
)

private val storeScreenshotModules = listOf(
    ":feature:reading_plan",
    ":feature:day",
    ":feature:day_study",
    ":feature:books",
    ":feature:read",
    ":feature:chat",
)

// The generators all write into one shared directory and never clean it, so a screen that gets
// renamed would otherwise leave its old file there and be collected forever.
private val clearGeneratedStoreScreenshots = tasks.register<Delete>("clearGeneratedStoreScreenshots") {
    delete(layout.buildDirectory.dir("outputs/store-screenshots"))
}

storeScreenshotModules.forEach { modulePath ->
    project(modulePath).tasks.matching { task -> task.name == "testAndroidHostTest" }.configureEach {
        dependsOn(clearGeneratedStoreScreenshots)
    }
}

tasks.register("collectStoreScreenshots") {
    group = "store-screenshots"
    description = "Regenerates every store screenshot and collects it into store_listings/."
    dependsOn(storeScreenshotModules.map { modulePath -> "$modulePath:testAndroidHostTest" })
    val generatedDir = layout.buildDirectory.dir("outputs/store-screenshots")
    val listingsDir = layout.projectDirectory.dir("store_listings")
    val locales = storeScreenshotLocales
    val devices = storeScreenshotDevices
    doLast {
        // Wiped first so a screen that was renamed or dropped cannot leave a stale image behind.
        listingsDir.asFile.deleteRecursively()
        var collected = 0
        val modules = generatedDir
            .get()
            .asFile
            .list()
            .orEmpty()
        locales.forEach { (locale, language) ->
            devices.forEach { (sourcePath, target) ->
                val (platform, device) = target
                // store_listings/<platform>/<language>/<device>/<screen>.png
                val destination = listingsDir.asFile.resolve("$platform/$language/$device")
                destination.mkdirs()
                modules.forEach { module ->
                    val source = generatedDir.get().asFile.resolve("$module/$locale/$sourcePath")
                    source.listFiles { file -> file.extension == "png" }?.forEach { png ->
                        png.copyTo(destination.resolve(png.name), overwrite = true)
                        collected++
                    }
                }
            }
        }
        logger.lifecycle("collectStoreScreenshots: collected $collected screenshot(s) into store_listings/")
    }
}

// Play and Apple each fan a single generated Spanish set out to their own regional codes, read off
// the two consoles: Play carries es-419/es-ES/es-US, App Store Connect carries es-MX/es-ES.
private val playStoreLocales = mapOf(
    "en-US" to listOf("en-US"),
    "pt-BR" to listOf("pt-BR"),
    "es" to listOf("es-419", "es-ES", "es-US"),
)

private val appStoreLocales = mapOf(
    "en-US" to listOf("en-US"),
    "pt-BR" to listOf("pt-BR"),
    "es" to listOf("es-MX", "es-ES"),
)

// Generator output directory to the images/ subdirectory Play expects it under.
private val playStoreDevices = mapOf(
    "images/phoneScreenshots" to "phoneScreenshots",
    "images/sevenInchScreenshots" to "sevenInchScreenshots",
    "images/tenInchScreenshots" to "tenInchScreenshots",
)

// deliver reads fastlane/screenshots/<locale>/*.png flat and infers the device from the image size,
// so the suffix only keeps one screen's iPhone and iPad shots from colliding.
private val appStoreDevices = mapOf(
    "iphone65" to "iphone_6_5",
    "iphone67" to "iphone_6_7",
    "ipad13" to "ipad_13",
    "ipad11" to "ipad_11",
)

tasks.register("stageStoreScreenshots") {
    group = "store-screenshots"
    description = "Regenerates every store screenshot into the layout fastlane uploads from."
    dependsOn(storeScreenshotModules.map { modulePath -> "$modulePath:testAndroidHostTest" })
    val generatedDir = layout.buildDirectory.dir("outputs/store-screenshots")
    val playDir = layout.projectDirectory.dir("fastlane/metadata/android")
    val appleDir = layout.projectDirectory.dir("fastlane/screenshots")
    val playLocales = playStoreLocales
    val appleLocales = appStoreLocales
    val playDevices = playStoreDevices
    val appleDevices = appStoreDevices
    doLast {
        var staged = 0
        val modules = generatedDir
            .get()
            .asFile
            .list()
            .orEmpty()
        playLocales.forEach { (locale, storeLocales) ->
            playDevices.forEach { (sourcePath, imagesDir) ->
                storeLocales.forEach { storeLocale ->
                    val destination = playDir.asFile.resolve("$storeLocale/images/$imagesDir")
                    destination.deleteRecursively()
                    destination.mkdirs()
                    modules.forEach { module ->
                        val source = generatedDir.get().asFile.resolve("$module/$locale/$sourcePath")
                        source.listFiles { file -> file.extension == "png" }?.forEach { png ->
                            png.copyTo(destination.resolve(png.name), overwrite = true)
                            staged++
                        }
                    }
                }
            }
        }
        appleLocales.forEach { (locale, storeLocales) ->
            storeLocales.forEach { storeLocale ->
                val destination = appleDir.asFile.resolve(storeLocale)
                destination.deleteRecursively()
                destination.mkdirs()
                appleDevices.forEach { (sourcePath, suffix) ->
                    modules.forEach { module ->
                        val source = generatedDir.get().asFile.resolve("$module/$locale/$sourcePath")
                        source.listFiles { file -> file.extension == "png" }?.forEach { png ->
                            png.copyTo(destination.resolve("${png.nameWithoutExtension}_$suffix.png"), overwrite = true)
                            staged++
                        }
                    }
                }
            }
        }
        logger.lifecycle("stageStoreScreenshots: staged $staged screenshot(s) for fastlane")
    }
}
