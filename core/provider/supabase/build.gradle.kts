import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.provider.supabase"
    }
    jvm()
    sourceSets {
        commonMain.dependencies {
            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.compose.auth.ui)

            // Ktor
            implementation(libs.ktor.client.core)

            // Serialization
            implementation(libs.kotlin.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
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
