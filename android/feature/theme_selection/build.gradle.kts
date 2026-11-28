plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.preferences.themeSelection)
    implementation(projects.core.model)
    implementation(projects.ui.theme)
    implementation(libs.compose.materialIconsExtended)
}
