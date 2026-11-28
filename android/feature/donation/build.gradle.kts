plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.donation)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
}
