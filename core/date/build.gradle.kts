import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
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
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }

        androidMain.dependencies {
            implementation(libs.koinAndroid)
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
