plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.android.gradle)
    implementation(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        create("androidCommonConfig") {
            id = "com.quare.android.common"
            implementationClass = "com.quare.blitzsplit.plugins.AndroidCommonConfigPlugin"
        }
        register("kotlinMultiplatform") {
            id = "bibleplanner.kotlin.multiplatform"
            implementationClass = "KotlinMultiplatformConventionPlugin"
        }
        register("composeMultiplatform"){
            id = "bibleplanner.kotlin.composeMultiplatform"
            implementationClass = "ComposeMultiplatformConventionPlugin"
        }
    }
}
