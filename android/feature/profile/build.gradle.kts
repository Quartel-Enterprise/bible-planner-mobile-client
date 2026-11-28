plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.profile)
    implementation(projects.core.model)
    implementation(projects.core.profile)
    implementation(projects.core.provider.billing)
    implementation(projects.core.preferences.studySuggestion)
    implementation(projects.core.utils)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.shimmer)
    implementation(libs.kotlinx.datetime)
}
