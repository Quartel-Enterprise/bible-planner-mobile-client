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

compose.desktop {
    application {
        mainClass = "com.quare.bibleplanner.MainKt"

        jvmArgs += listOf("-Xdock:icon=${project.file("../icons/bible_planner_logo.icns").absolutePath}")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "com.quare.bibleplanner"
            packageVersion = project.property("versionName").toString()

            // Output of `./gradlew :desktopApp:suggestRuntimeModules` (jdeps). Without
            // jdk.unsupported the jlink image has no sun.misc.Unsafe, and DataStore's
            // bundled protobuf dies with NoClassDefFoundError on the first write.
            modules("java.instrument", "java.management", "java.prefs", "jdk.security.auth", "jdk.unsupported")

            macOS {
                iconFile.set(project.file("../icons/bible_planner_logo.icns"))
                // CFBundleVersion, kept in step with the iOS CURRENT_PROJECT_VERSION so both
                // platforms ship the same build number from version.xcconfig.
                packageBuildVersion = project.property("versionCode").toString()
            }

            windows {
                iconFile.set(project.file("../icons/bible_planner_logo.ico"))
            }

            linux {
                iconFile.set(project.file("../icons/bible_planner_logo.png"))
            }
        }
    }
}
