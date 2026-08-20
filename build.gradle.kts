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
