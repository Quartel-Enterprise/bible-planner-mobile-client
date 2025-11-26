plugins {
    alias(libs.plugins.kotlinMultiplatformConvention)
    alias(libs.plugins.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Serialization
            implementation(libs.kotlin.serialization.json)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.core.model"
}
