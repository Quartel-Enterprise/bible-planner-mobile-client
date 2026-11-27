plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.ads"
        withHostTest {}
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {}

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)
            implementation(projects.core.provider.platform)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(projects.core.provider.ads.testing)
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val nonMobileMain = create("nonMobileMain") {
            dependsOn(commonMain.get())
        }

        jvmMain {
            dependsOn(nonMobileMain)
        }

        webMain {
            dependsOn(nonMobileMain)
        }

        // Why: the manual dependsOn edges above turn off the default hierarchy, so both web targets are wired here.
        jsMain {
            dependsOn(webMain.get())
        }
        wasmJsMain {
            dependsOn(webMain.get())
        }

        iosMain {
            dependsOn(commonMain.get())
        }
        iosArm64Main {
            dependsOn(iosMain.get())
        }
        iosSimulatorArm64Main {
            dependsOn(iosMain.get())
        }

        androidMain.dependencies {
            implementation(libs.play.services.ads)
            implementation(libs.user.messaging.platform)
            implementation(libs.koin.android)
        }
    }
}

dependencies {
    constraints {
        "androidMainImplementation"(libs.play.hsdp)
    }
}
