import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.bibleplanner.buildlogic.configureCoverage
import com.bibleplanner.buildlogic.getAndroidSdkVersions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            val sdkVersions = getAndroidSdkVersions()

            // Configure Kotlin Multiplatform extension
            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>(
                    "androidLibrary",
                ) {
                    compileSdk = sdkVersions.compileSdk
                    minSdk = sdkVersions.minSdk
                    experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
                }

                // Configure iOS targets
                listOf(
                    iosArm64(), // for ios devices
                    iosSimulatorArm64(), // for ios simulators in Apple silicon Mac computer
                ).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = path.substring(1).replace(':', '-')
                    }
                }

                // Why: Kotlin/Wasm can't export generic classes, fun interfaces or lambdas, so the React web
                // app reaches the ViewModels through the js target; wasmJs keeps the Compose web app running.
                js {
                    browser()
                    // Why: ES modules keep import.meta.url, which the Room worker URL and Vite's bundling need.
                    useEsModules()
                }

                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                }

                // remove expect actual warning
                compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
            }

            configureCoverage()

            pluginManager.withPlugin("com.codingfeline.buildkonfig") {
                tasks.matching { it.name.contains("ArtProfile") }.configureEach {
                    dependsOn("generateBuildKonfig")
                }
            }
        }
    }
}
