plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.serialization)
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
