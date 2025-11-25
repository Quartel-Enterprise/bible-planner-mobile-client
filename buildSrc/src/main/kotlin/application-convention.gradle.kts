import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

val androidNamespace = project.findProperty("android.namespace") as String?
    ?: (project.group.toString() + "." + project.name.replace("-", "."))

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = project.name
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.findLibrary("androidx-activity-compose").get())
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.findLibrary("kotlinx-coroutinesSwing").get())
        }
    }
}

android {
    namespace = androidNamespace
    compileSdk = (project.findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        applicationId = project.findProperty("android.applicationId") as? String
            ?: androidNamespace
        minSdk = (project.findProperty("android.minSdk") as String).toInt()
        targetSdk = (project.findProperty("android.targetSdk") as String).toInt()
        versionCode = (project.findProperty("android.versionCode") as String).toInt()
        versionName = project.findProperty("android.versionName") as String
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = project.findProperty("compose.desktop.mainClass") as String?
            ?: "$androidNamespace.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = project.findProperty("compose.desktop.packageName") as String?
                ?: androidNamespace
            packageVersion = project.findProperty("compose.desktop.packageVersion") as String?
                ?: "1.0.0"
        }
    }
}
