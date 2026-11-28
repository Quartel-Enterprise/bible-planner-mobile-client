plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.main)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.profile)
    implementation(projects.core.provider.analytics)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.coil.compose)
}
