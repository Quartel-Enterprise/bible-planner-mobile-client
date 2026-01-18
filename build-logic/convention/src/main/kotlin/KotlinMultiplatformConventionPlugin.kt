import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.bibleplanner.buildlogic.getAndroidSdkVersions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        val sdkVersions = getAndroidSdkVersions()

        // Configure Kotlin Multiplatform extension
        extensions.configure<KotlinMultiplatformExtension> {

            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
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

            //remove expect actual warning
            compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}
