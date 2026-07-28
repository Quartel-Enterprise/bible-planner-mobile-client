import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.crashlytics"
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {}

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.crashlytics)
            implementation(libs.koin.android)
        }

        jvmMain.dependencies {
            implementation(libs.sentry)
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

buildkonfig {
    packageName = "com.quare.bibleplanner.core.provider.crashlytics.generated"
    objectName = "CrashlyticsBuildKonfig"
    exposeObjectWithName = "CrashlyticsBuildKonfig"

    defaultConfigs {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val sentryDsn = properties.getProperty("SENTRY_DSN").orEmpty()
        if (sentryDsn.isBlank()) {
            logger.warn("⚠️ SENTRY_DSN not found in local.properties. Desktop crash reporting will be disabled.")
        }
        buildConfigField(FieldSpec.Type.STRING, "SENTRY_DSN", sentryDsn)
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}
