plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.room"
    }

    jvm("desktop")
    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Room
            implementation(libs.androidx.room.runtime)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }

        androidMain.dependencies {
            // Framework SQLite driver: relies on the OS-provided SQLite so the
            // Android app does not ship/load the bundled libsqliteJni.so native
            // library, which can be missing when an AAB ABI split fails to install.
            implementation(libs.androidx.sqlite.framework)
        }

        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

// Skip iOS compilation during ktlint to avoid requiring KSP-generated code for iOS
// iOS KSP tasks may be skipped in CI without native toolchains, causing compilation failures
afterEvaluate {
    // Disable iOS compilation tasks when skipIosBuild property is set
    // This is used during ktlint to avoid native toolchain requirements
    val skipIosBuild = project.findProperty("skipIosBuild") == "true"

    if (skipIosBuild) {
        tasks
            .matching {
                it.name.startsWith("compileKotlinIos")
            }.configureEach {
                enabled = false
            }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}
