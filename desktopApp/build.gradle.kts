import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Sync
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.compose.hotReload)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(projects.shared)

    // Compose
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
    implementation(libs.kotlinx.coroutines.swing)

    // Koin
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.core)

    // Room — getDatabaseBuilder() exposes RoomDatabase.Builder in its signature
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)
}

// BundledSQLiteDriver extracts libsqliteJni.dylib to a writable directory and dlopen()s it.
// Under the macOS App Sandbox, files created by the process are auto-quarantined and dlopen
// of quarantined code hangs forever (Gatekeeper with no UI), deadlocking Room on the first
// writer connection. Shipping the dylib inside the app image and putting it on
// java.library.path makes the loader's System.loadLibrary attempt succeed before the
// extract-and-load fallback runs. Harmless outside the sandbox (issue #204 spike).
val extractSqliteNativeLibraries = tasks.register<Sync>("extractSqliteNativeLibraries") {
    description =
        "Extracts the bundled SQLite JNI library so it ships inside the app image instead of being extracted at runtime."
    from(
        configurations.runtimeClasspath.map { classpath ->
            classpath
                .filter { file -> file.name.startsWith("sqlite-bundled-jvm") }
                .map(::zipTree)
        },
    ) {
        include("natives/osx_arm64/*")
        eachFile {
            relativePath = RelativePath(true, "macos-arm64", name)
        }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("sqliteNatives"))
}

// Mac App Store variant, selected with `-Pmas` (e.g. `./gradlew :desktopApp:packagePkg -Pmas`).
// It packages a sandboxed .pkg with the same bundle ID as the iOS app, which is required for
// App Store Connect (same app record) and RevenueCat universal purchases. The regular build
// (direct .dmg/.msi/.deb/.rpm) is untouched when the property is absent.
val isMacAppStoreBuild = providers.gradleProperty("mas").isPresent

compose.desktop {
    application {
        mainClass = "com.quare.bibleplanner.MainKt"

        // jpackage bundles the JDK that builds the app. Homebrew JDKs link libfontmanager
        // against /opt/homebrew, which is unreachable from the macOS App Sandbox and from any
        // machine without Homebrew, so packaging must use a self-contained JDK (Temurin,
        // Corretto). Point DESKTOP_PACKAGING_JAVA_HOME at one; the Gradle JVM is used otherwise.
        providers.environmentVariable("DESKTOP_PACKAGING_JAVA_HOME").orNull?.let { javaHome = it }

        jvmArgs += listOf("-Xdock:icon=${project.file("../icons/bible_planner_logo.icns").absolutePath}")
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            // $APPDIR is expanded by the jpackage launcher at runtime; during `:desktopApp:run`
            // the literal path simply misses and the driver falls back to extraction, which is
            // fine outside the sandbox.
            jvmArgs += listOf($$"-Djava.library.path=$APPDIR/resources:$APPDIR")
        }

        nativeDistributions {
            if (isMacAppStoreBuild) {
                targetFormats(TargetFormat.Pkg)
            } else {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            }
            // Names the .app/.msi/.deb on disk. Kept space-free like the iOS PRODUCT_NAME so
            // installer file names stay shell- and GitHub-release-friendly; the spaced name
            // users actually see comes from CFBundleDisplayName below.
            packageName = "BiblePlanner"
            packageVersion = project.property("versionName").toString()

            // Output of `./gradlew :desktopApp:suggestRuntimeModules` (jdeps). Without
            // jdk.unsupported the jlink image has no sun.misc.Unsafe, and DataStore's
            // bundled protobuf dies with NoClassDefFoundError on the first write.
            modules("java.instrument", "java.management", "java.prefs", "jdk.security.auth", "jdk.unsupported")

            appResourcesRootDir.set(layout.dir(extractSqliteNativeLibraries.map(Sync::getDestinationDir)))

            macOS {
                iconFile.set(project.file("../icons/bible_planner_logo.icns"))
                // Pinned because the plugin otherwise derives it from packageName, and the
                // already-released .dmg identifies itself as com.quare.bibleplanner.
                bundleID = "com.quare.bibleplanner"
                // jpackage defaults to 10.13, which makes the App Store demand a universal
                // binary. jlink only emits the host architecture, so the runtime is arm64-only;
                // Apple allows that as long as the deployment target is 12.0 or higher.
                minimumSystemVersion = "12.0"
                // CFBundleVersion. App Store Connect rejects a build whose value was already
                // uploaded, so it tracks versionCode — the same number iOS ships as
                // CURRENT_PROJECT_VERSION, bumped on every release by scripts/bump-version.sh.
                packageBuildVersion = project.property("versionCode").toString()
                // Without the export compliance declaration App Store Connect holds the build
                // back and never offers it to TestFlight groups. The app only uses HTTPS, which
                // is exempt — same declaration iosApp/iosApp/Info.plist already carries.
                infoPlist {
                    extraKeysRawXml =
                        """
                        <key>ITSAppUsesNonExemptEncryption</key>
                        <false/>
                        <key>CFBundleDisplayName</key>
                        <string>Bible Planner</string>
                        """.trimIndent()
                }
                if (isMacAppStoreBuild) {
                    // Same bundle ID as iosApp/Configuration/Config.xcconfig, mandatory for the
                    // shared App Store Connect record and RevenueCat universal purchases.
                    bundleID = "com.quare.bibleplanner.BiblePlanner"
                    appStore = true
                    appCategory = "public.app-category.reference"
                    entitlementsFile.set(project.file("macos/mas.entitlements"))
                    runtimeEntitlementsFile.set(project.file("macos/mas-runtime.entitlements"))
                    // The plugin copies each profile into the bundle keeping its file name, and
                    // codesign only accepts Contents/embedded.provisionprofile — hence the fixed
                    // name inside per-target directories.
                    val appProfile = project.file("macos/app/embedded.provisionprofile")
                    if (appProfile.exists()) {
                        provisioningProfile.set(appProfile)
                    }
                    val runtimeProfile = project.file("macos/runtime/embedded.provisionprofile")
                    if (runtimeProfile.exists()) {
                        runtimeProvisioningProfile.set(runtimeProfile)
                    }
                    val signingIdentity = providers.environmentVariable("MAC_APP_STORE_SIGNING_IDENTITY")
                    if (signingIdentity.isPresent) {
                        signing {
                            sign.set(true)
                            identity.set(signingIdentity.get())
                        }
                    }
                }
            }

            windows {
                iconFile.set(project.file("../icons/bible_planner_logo.ico"))
            }

            linux {
                iconFile.set(project.file("../icons/bible_planner_logo.png"))
                // Debian/RPM package names must be lowercase without spaces, so they can't
                // inherit the display name.
                packageName = "bible-planner"
            }
        }
    }
}
