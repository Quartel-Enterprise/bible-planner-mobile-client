plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidCommonConfig)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.model"
    }
    jvm()
    sourceSets {
        commonMain.dependencies {
            // DateTime
            implementation(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlin.serialization.json)
        }
    }
}
