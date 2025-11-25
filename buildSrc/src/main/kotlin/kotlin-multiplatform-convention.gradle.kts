import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    androidTarget()
    
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
        commonMain.dependencies {
            // Add common dependencies here
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}

android {
    namespace = project.findProperty("android.namespace") as String?
        ?: (project.group.toString() + "." + project.name.replace("-", "."))
    compileSdk = (project.findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (project.findProperty("android.minSdk") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}


