plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.quare.bibleplanner"
    compileSdk = libs.versions.android.compileSdk
        .get()
        .toInt()
    defaultConfig {
        applicationId = "com.quare.bibleplanner"
        minSdk = libs.versions.android.minSdk
            .get()
            .toInt()
        targetSdk = libs.versions.android.targetSdk
            .get()
            .toInt()
        versionCode = project.property("versionCode").toString().toInt()
        versionName = project.property("versionName").toString()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(projects.composeApp)

    // Features
    implementation(projects.feature.materialYou)
    implementation(projects.feature.themeSelection)
    implementation(projects.feature.readingPlan)
    implementation(projects.feature.day)
    implementation(projects.feature.deleteProgress)
    implementation(projects.feature.editPlanStartDate)

    // Core
    implementation(projects.core.books)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.provider.koin)
    implementation(projects.core.provider.room)
    implementation(projects.core.utils)
    implementation(projects.core.date)
    implementation(projects.core.plan)
    implementation(projects.core.provider.dataStore)
    implementation(projects.core.provider.platform)

    // UI
    implementation(projects.ui.theme)
    implementation(projects.ui.utils)
    implementation(projects.ui.component)

    // DB
    implementation(libs.androidx.room.runtime)

    // Koin
    implementation(project.dependencies.platform(libs.koinBom))
    implementation(libs.koinAndroid)

    // Facebook SDK
    implementation(libs.facebook.sdk.android)

    // Compose
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(libs.androidx.activity.compose)
}
