import com.android.build.api.dsl.LibraryExtension
import com.bibleplanner.buildlogic.getAndroidSdkVersions
import com.bibleplanner.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/*
 * Why: the native Android UI lives in :android:* library modules (Blitz Split's layout), apart from the
 * shared feature modules that iOS and the web still draw with Compose Multiplatform until step 4.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply(
                    libs
                        .findPlugin("compose-compiler")
                        .get()
                        .get()
                        .pluginId,
                )
            }

            val sdkVersions = getAndroidSdkVersions()
            extensions.configure<LibraryExtension> {
                namespace = createNamespace()
                compileSdk = sdkVersions.compileSdk
                defaultConfig {
                    minSdk = sdkVersions.minSdk
                }
            }

            dependencies {
                listOf(
                    "compose-runtime",
                    "compose-foundation",
                    "androidx-compose-material3",
                    "compose-ui",
                    "compose-ui-toolingPreview",
                    "compose-materialIconsCore",
                ).forEach { alias -> add("implementation", libs.findLibrary(alias).get()) }
                add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
                // Why: a feature screen builds its Navigation 3 entry with the shared ViewModel from Koin.
                if (path.startsWith(FEATURE_PATH_PREFIX)) {
                    checkNativeEntryFactoryIsListed()
                    add("implementation", platform(libs.findLibrary("koin-bom").get()))
                    add("implementation", libs.findLibrary("koin-compose-viewmodel").get())
                    add("implementation", libs.findLibrary("navigation3-ui").get())
                    add("implementation", project(DESIGN_SYSTEM_PATH))
                    add("implementation", project(NAVIGATION_PATH))
                }
            }
        }
    }

    // Why: the app finds native screens with ServiceLoader, so an unlisted factory would silently keep the shared UI.
    private fun Project.checkNativeEntryFactoryIsListed() {
        val servicesFile = file("src/main/resources/META-INF/services/$NATIVE_ENTRY_FACTORY")
        val servicesPath = servicesFile.relativeTo(rootDir)
        check(servicesFile.isFile) {
            "$path must list its NativeEntryFactory implementation in $servicesPath"
        }
        // Why: a services file copied from another screen's module would point at that screen's factory instead.
        val namespace = createNamespace()
        val listedFactories = servicesFile
            .readLines()
            .map { line -> line.substringBefore('#').trim() }
            .filter { line -> line.isNotEmpty() }
        check(listedFactories.isNotEmpty() && listedFactories.all { factory -> factory.startsWith("$namespace.") }) {
            "$servicesPath must list this module's own NativeEntryFactory implementation, under package $namespace"
        }
    }

    // Why: :android:feature:delete_notes becomes com.quare.bibleplanner.android.feature.deletenotes, the package.
    private fun Project.createNamespace(): String = "com.quare.bibleplanner" +
        path
            .replace(
                oldChar = ':',
                newChar = '.',
            ).replace(
                oldValue = "_",
                newValue = "",
            )

    private companion object {
        const val DESIGN_SYSTEM_PATH = ":android:designsystem"
        const val NAVIGATION_PATH = ":android:navigation"
        const val NATIVE_ENTRY_FACTORY = "com.quare.bibleplanner.android.navigation.NativeEntryFactory"
        const val FEATURE_PATH_PREFIX = ":android:feature:"
    }
}
