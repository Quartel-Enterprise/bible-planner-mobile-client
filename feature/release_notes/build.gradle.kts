import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.feature.release_notes"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.utils.jsonReader)
            implementation(projects.core.network)
            implementation(projects.core.date)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.theme)
            implementation(projects.ui.utils)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Navigation
            implementation(libs.compose.navigation)

            // Ktor
            implementation(libs.ktor.client.core)
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.feature.releasenotes.generated"
    objectName = "ReleaseNotesBuildKonfig"
    exposeObjectWithName = "ReleaseNotesBuildKonfig"

    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val ghToken = "GH_TOKEN"
        val value = properties.getProperty(ghToken).orEmpty()

        buildConfigField(FieldSpec.Type.STRING, ghToken, value)

        if (!properties.containsKey(ghToken) || value.isBlank() || value == "your_token_here") {
            logger.warn(
                "⚠️ $ghToken not found or not configured in local.properties. Release notes dates will not be fetched from GitHub.",
            )
        }
    }
}
