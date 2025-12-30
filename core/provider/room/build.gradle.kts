plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    androidLibrary {
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
            implementation(libs.androidx.sqlite.bundled)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
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

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}
