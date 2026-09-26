import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.inappupdate"
        withHostTest {}
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.date)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.utils)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Logging
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.play.app.update)
            implementation(libs.play.app.update.ktx)
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.inappupdate.generated"
    objectName = "InAppUpdateBuildKonfig"
    exposeObjectWithName = "InAppUpdateBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}
