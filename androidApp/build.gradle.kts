plugins {
    alias(libs.plugins.androidApplication)
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
    signingConfigs {
        create("release") {
            /*
             * Populated only on CI, where the release workflow exports these
             * variables after decoding the keystore from GitHub secrets.
             * Local builds leave them unset and fall back to debug signing.
             */
            val keystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (System.getenv("ANDROID_KEYSTORE_PATH") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            /*
             * Bundle native debug symbols into the AAB so Google Play can
             * symbolicate native crash stack traces. Stripped from the user
             * download, so it has no impact on the delivered app size.
             */
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
}

dependencies {
    implementation(projects.composeApp)

    // Features
    implementation(projects.feature.materialYou)
    implementation(projects.feature.preferences.themeSelection)
    implementation(projects.feature.preferences.appLanguage)
    implementation(projects.feature.readingPlan)
    implementation(projects.feature.day)
    implementation(projects.feature.deleteProgress)
    implementation(projects.feature.preferences.editPlanStartDate)
    implementation(projects.feature.login)

    // Core
    implementation(projects.core.books)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.provider.koin)
    implementation(projects.core.provider.language)
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

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Compose
    implementation(libs.material3)
    implementation(libs.ui)
    implementation(libs.components.resources)
    implementation(libs.androidx.activity.compose)
}
