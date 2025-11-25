import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.applicationConvention)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

// Override namespace and applicationId
android {
    namespace = "quare.software.bibleplanner"
    defaultConfig {
        applicationId = "quare.software.bibleplanner"
    }
}

// Add project-specific dependencies
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Theme
            implementation(projects.ui.theme)
        }
    }
}

// Override compose.desktop configuration
compose.desktop {
    application {
        mainClass = "quare.software.bibleplanner.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "quare.software.bibleplanner"
            packageVersion = "1.0.0"
        }
    }
}
