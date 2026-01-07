import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.buildkonfig)
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
    jvm()
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.provider.billing"
    }

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
                implementation(project.dependencies.platform(libs.koinBom))
                implementation(libs.koinCore)
                implementation(libs.kotlinx.coroutines.core)
                implementation(projects.core.remoteConfig)
                implementation(libs.kotlinx.datetime)
                implementation(projects.core.date)
            }
        }

        val mobileMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.purchases.core)
                api(libs.purchases.result)
            }
        }

        val androidMain by getting {
            dependsOn(mobileMain)
            dependencies {
                implementation(project.dependencies.platform(libs.firebase.bom))
                api(libs.purchases.core)
                api(libs.purchases.result)
            }
        }

        val iosMain by creating {
            dependsOn(mobileMain)
            dependencies {
                implementation(libs.purchases.core)
                implementation(libs.purchases.result)
            }
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val jvmMain by getting
    }
}
