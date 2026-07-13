import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.gradle.api.tasks.PathSensitivity
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.analytics"
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {}

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.kotlinx.coroutines.core)
            implementation(projects.core.model)
            implementation(projects.core.user)
            implementation(projects.core.remoteConfig)
            implementation(libs.kotlin.serialization.json)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.koinAndroid)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.kermit)
            implementation(projects.core.utils)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

tasks.withType<Test>().configureEach {
    if (name == "jvmTest") {
        inputs
            .files(
                fileTree(rootProject.layout.projectDirectory.dir("feature")) {
                    include("**/*.kt")
                    exclude("**/build/**")
                },
            ).withPropertyName("featureUiEventSources")
            .withPathSensitivity(PathSensitivity.RELATIVE)
        inputs
            .dir(rootProject.layout.projectDirectory.dir("docs/analytics/events"))
            .withPropertyName("analyticsEventsCatalog")
            .withPathSensitivity(PathSensitivity.RELATIVE)
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.provider.analytics.generated"
    objectName = "AnalyticsBuildKonfig"
    exposeObjectWithName = "AnalyticsBuildKonfig"

    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val propertiesKeys = listOf("GA_MEASUREMENT_ID", "GA_MEASUREMENT_API_SECRET")
        propertiesKeys.forEach { propertyKey ->
            val propertyValue = properties.getProperty(propertyKey).orEmpty()
            if (propertyValue.isBlank()) {
                logger.warn("⚠️ $propertyKey not found in local.properties. Desktop analytics will be disabled.")
            }
            buildConfigField(FieldSpec.Type.STRING, propertyKey, propertyValue)
        }
    }
}
