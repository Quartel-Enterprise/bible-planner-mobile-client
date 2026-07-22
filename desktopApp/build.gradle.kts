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
            packageVersion = "2.1.1"

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
