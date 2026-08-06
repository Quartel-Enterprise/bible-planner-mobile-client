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

        iosMain {
            dependsOn(commonMain.get())
        }
        iosArm64Main {
            dependsOn(iosMain.get())
        }
        iosSimulatorArm64Main {
            dependsOn(iosMain.get())
        }
    }
}

val sentryDsn: String = run {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    properties.getProperty("SENTRY_DSN").orEmpty()
}

buildkonfig {
    packageName = "com.quare.bibleplanner.core.provider.crashlytics.generated"
    objectName = "CrashlyticsBuildKonfig"
    exposeObjectWithName = "CrashlyticsBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "SENTRY_DSN", sentryDsn)
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}

// Only the JVM target reads SENTRY_DSN, so the warning belongs to that compilation
// instead of firing on every Android and iOS build that configures this module.
tasks.named("compileKotlinJvm") {
    // Copied into a local so doFirst captures the flag, not the enclosing build script,
    // which the configuration cache cannot serialize.
    val isSentryDsnMissing = sentryDsn.isBlank()
    doFirst {
        if (isSentryDsnMissing) {
            logger.warn("⚠️ SENTRY_DSN not found in local.properties. Desktop crash reporting will be disabled.")
        }
    }
}
