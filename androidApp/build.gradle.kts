import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

/*
 * Populated only by the release workflow, which exports it after decoding the keystore from GitHub
 * secrets. Every other release build (build-and-test, local runs) is a compile check nobody ships.
 */
val releaseKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")

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
            if (releaseKeystorePath != null) {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (releaseKeystorePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            /*
             * Only a shipped build needs its crashes tied back to a mapping file and a commit. Both
             * change on every build (the mapping file id is random, the commit is the pull request's
             * merge commit) and both land in the resources R8 shrinks, so leaving them in the
             * builds nobody ships would keep R8 — most of the build job — from ever coming from
             * the build cache. It also stops those builds from uploading their mapping files.
             */
            vcsInfo {
                include = releaseKeystorePath != null
            }
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = releaseKeystorePath != null
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
    implementation(projects.shared)

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
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Compose
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.androidx.activity.compose)
}
