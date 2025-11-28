import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

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
