import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.date"
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Modules
            implementation(projects.core.network)
            implementation(projects.core.utils)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Date
            implementation(libs.kotlinx.datetime)

            // Ktor
            implementation(libs.ktor.client.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.play.services.time)
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.date"

    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val propertyKey = "SUPABASE_URL"
        val propertyValue = properties.getProperty(propertyKey).orEmpty()
        if (propertyValue.isBlank()) {
            logger.warn("⚠️ $propertyKey not found in local.properties.")
        }
        buildConfigField(FieldSpec.Type.STRING, propertyKey, propertyValue)
    }
}
