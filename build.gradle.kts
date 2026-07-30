import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        version.set("1.8.0")
        debug.set(false)
        verbose.set(true)
        android.set(true)
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

    // Ensure KSP code generation runs before ktlint for projects with KSP
    // This is important for Room's DatabaseConstructor and other generated code
    afterEvaluate {
        val ktlintCheckTask = tasks.findByName("ktlintCheck")
        if (ktlintCheckTask != null) {
            // Make ktlintCheck depend on KSP tasks if they exist
            // Priority: Android and JVM first (don't require native toolchains in CI)
            // These are the most important for generating code that ktlint needs
            val primaryKspTaskNames = listOf(
                "kspKotlinAndroid",
                "kspKotlinJvm",
            )

            // Also try iOS targets, but they may fail in CI without native toolchains
            val secondaryKspTaskNames = listOf(
                "kspKotlinMetadata",
                "kspKotlinIosArm64",
                "kspKotlinIosSimulatorArm64",
            )

            // Add dependencies for primary targets (required)
            primaryKspTaskNames.forEach { taskName ->
                val kspTask = tasks.findByName(taskName)
                if (kspTask != null) {
                    ktlintCheckTask.dependsOn(kspTask)
                }
            }

            // Add dependencies for secondary targets (optional - may fail in CI)
            secondaryKspTaskNames.forEach { taskName ->
                val kspTask = tasks.findByName(taskName)
                if (kspTask != null) {
                    ktlintCheckTask.dependsOn(kspTask)
                }
            }
        }
    }
}
