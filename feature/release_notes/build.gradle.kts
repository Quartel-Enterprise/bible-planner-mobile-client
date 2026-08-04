import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.release_notes"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.platform)
            implementation(projects.core.utils)
            implementation(projects.core.utils.jsonReader)
            implementation(projects.core.network)
            implementation(projects.core.date)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.theme)
            implementation(projects.ui.utils)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation 3
            implementation(libs.navigation3.ui)

            // Ktor
            implementation(libs.ktor.client.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
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
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())

        if (!properties.containsKey(ghToken) || value.isBlank() || value == "your_token_here") {
            logger.warn(
                "⚠️ $ghToken not found or not configured in local.properties. Release notes dates will not be fetched from GitHub.",
            )
        }
    }
}
