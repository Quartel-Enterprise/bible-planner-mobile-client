import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.review"
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.date)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.dataStore)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.review.generated"
    objectName = "ReviewBuildKonfig"
    exposeObjectWithName = "ReviewBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}
