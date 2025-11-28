plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.androidCommonConfig)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreBooks"
            isStatic = true
        }
    }

    jvm()
    
    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.room)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlin.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
        
        // Ensure iOS source sets include resources from commonMain
        getByName("iosArm64Main") {
            resources.srcDirs("src/commonMain/resources")
        }
        getByName("iosSimulatorArm64Main") {
            resources.srcDirs("src/commonMain/resources")
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.core.books"
}

// Copy commonMain assets to androidMain before build
tasks.register<Copy>("copyCommonMainAssets") {
    from("src/commonMain/resources/assets")
    into("src/androidMain/assets")
    include("**/*.json")
}

// Ensure assets are copied before any Android build tasks
afterEvaluate {
    tasks.named("preBuild").configure {
        dependsOn("copyCommonMainAssets")
    }
}
