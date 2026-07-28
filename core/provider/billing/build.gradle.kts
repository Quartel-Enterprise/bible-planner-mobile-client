import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlin.serialization)
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.provider.billing"
    // defaults that can be overridden
    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val revenueKeys = listOf(
            "REVENUECAT_TEST_API_KEY",
            "REVENUECAT_APP_STORE_API_KEY",
            "REVENUECAT_PLAY_STORE_API_KEY",
            "REVENUECAT_WEB_BILLING_API_KEY",
            "REVENUECAT_WEB_PURCHASE_LINK",
            "REVENUECAT_WEB_BILLING_SANDBOX_API_KEY",
            "REVENUECAT_WEB_PURCHASE_LINK_SANDBOX",
        )
        revenueKeys.forEach { name ->
            buildConfigField(
                type = FieldSpec.Type.STRING,
                name = name,
                value = properties.getProperty(name).orEmpty(),
            )
            if (!properties.containsKey(name)) {
                logger.warn(getRevenueErrorMessage(name))
            }
        }
    }
}

private fun getRevenueErrorMessage(keyName: String): String =
    "⚠️ $keyName not found in local.properties. RevenueCat features will not work correctly. See docs/setup_revenuecat.md for setup instructions."
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.billing"
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {}

    sourceSets {

        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(projects.core.remoteConfig)
                implementation(projects.core.user)
                implementation(libs.kotlinx.datetime)
                implementation(projects.core.date)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val mobileMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.revenuecat.core)
                api(libs.revenuecat.result)
            }
        }

        val androidMain by getting {
            dependsOn(mobileMain)
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
            }
        }

        val iosMain by creating {
            dependsOn(mobileMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(projects.core.utils)
            implementation(projects.core.provider.platform)
        }
    }
}
