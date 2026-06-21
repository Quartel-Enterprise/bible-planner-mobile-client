plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.crashlytics"
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {}

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics)
            implementation(libs.koinAndroid)
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}
