plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.preferences.studySuggestion)
    implementation(projects.core.model)
    implementation(projects.core.preferences.studySuggestion)
    implementation(projects.ui.utils)
    implementation(libs.quare.loadable)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.shimmer)
}
