plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.chapterlistening"
        withHostTest {}
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {}

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.date)
            implementation(projects.core.model)
            implementation(projects.core.plan)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.utils)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.billing)
            implementation(projects.core.provider.dataStore)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // DataStore
            implementation(libs.datastore.preferences)

            // Compose Resources, for the book names the system player shows
            implementation(libs.compose.components.resources)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)

            // Shared fakes
            implementation(projects.core.books.testing)
            implementation(projects.core.plan.testing)
            implementation(projects.core.provider.dataStore.testing)
        }

        jvmTest.dependencies {
            // Skiko native library, required by compose-resources' getString() on the JVM target
            implementation(compose.desktop.currentOs)
        }

        val nonMobileMain = create("nonMobileMain") {
            dependsOn(commonMain.get())
        }

        jvmMain {
            dependsOn(nonMobileMain)
        }

        wasmJsMain {
            dependsOn(nonMobileMain)
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
            implementation(libs.androidx.media3.session)
            implementation(libs.koin.android)
        }
    }
}
