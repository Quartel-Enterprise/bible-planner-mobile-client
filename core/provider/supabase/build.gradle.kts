import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.supabase"
    }
    jvm()
    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.date)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.utils)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.compose.authUi)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.functions)

            // Ktor
            implementation(libs.ktor.client.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.provider.supabase.generated"
    objectName = "SupabaseBuildKonfig"
    exposeObjectWithName = "SupabaseBuildKonfig"

    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val propertiesKeys = listOf("SUPABASE_URL", "SUPABASE_API_KEY", "SUPABASE_GOOGLE_WEB_CLIENT_ID")
        propertiesKeys.forEach { propertyKey ->
            val propertyValue = properties.getProperty(propertyKey).orEmpty()
            if (propertyValue.isBlank()) {
                logger.warn("⚠️ $propertyKey not found in local.properties.")
            }
            buildConfigField(FieldSpec.Type.STRING, propertyKey, propertyValue)
        }
    }
}
