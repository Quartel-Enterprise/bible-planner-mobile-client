import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.feature.donation"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.utils)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            implementation(libs.compose.navigation)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
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
