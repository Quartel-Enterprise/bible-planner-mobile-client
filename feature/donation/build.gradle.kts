import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.donation"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.utils)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.feature.donation.generated"
    objectName = "DonationBuildKonfig"
    exposeObjectWithName = "DonationBuildKonfig"

    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField(FieldSpec.Type.STRING, "BTC_ONCHAIN", properties.getProperty("BTC_ONCHAIN").orEmpty())
        buildConfigField(FieldSpec.Type.STRING, "BTC_LIGHTNING", properties.getProperty("BTC_LIGHTNING").orEmpty())
        buildConfigField(FieldSpec.Type.STRING, "USDT_ERC20", properties.getProperty("USDT_ERC20").orEmpty())
        buildConfigField(FieldSpec.Type.STRING, "USDT_TRC20", properties.getProperty("USDT_TRC20").orEmpty())
        buildConfigField(FieldSpec.Type.STRING, "PIX_KEY", properties.getProperty("PIX_KEY").orEmpty())
    }
}
