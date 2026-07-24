import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

// Only the local dev run tasks are flagged as debug, so a packaged distribution defaults to
// crash reporting enabled. Flagging production instead would silently disable it if the flag
// were ever missed while packaging.
tasks.withType<JavaExec>().matching { it.name in setOf("run", "hotRun") }.configureEach {
    systemProperty("bibleplanner.debug", true)
}

dependencies {
    implementation(projects.shared)

    // Compose
    implementation(compose.desktop.currentOs)
    implementation(libs.components.resources)
    implementation(libs.kotlinx.coroutines.swing)

    // Koin
    implementation(project.dependencies.platform(libs.koinBom))
    implementation(libs.koinCore)

    // Room — getDatabaseBuilder() exposes RoomDatabase.Builder in its signature
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.bundled)
}

compose.desktop {
    application {
        mainClass = "com.quare.bibleplanner.MainKt"

        jvmArgs += listOf("-Xdock:icon=${project.file("../icons/bible_planner_logo.icns").absolutePath}")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.quare.bibleplanner"
            packageVersion = "2.2.0"

            macOS {
                iconFile.set(project.file("../icons/bible_planner_logo.icns"))
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
