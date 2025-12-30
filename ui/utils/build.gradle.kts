plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidCommonConfig)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget()


    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "UiUtils"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // View Model
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}



android {
    namespace = "com.quare.bibleplanner.ui.utils"
    compileSdk = project.property("compileSdkVersion").toString().toInt()
    
    defaultConfig {
        minSdk = project.property("minSdkVersion").toString().toInt()
    }
}
