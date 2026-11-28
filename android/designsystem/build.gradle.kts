plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(libs.compose.materialIconsExtended)
    implementation(projects.core.date)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.datetime)
}
