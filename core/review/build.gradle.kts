import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
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

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
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
