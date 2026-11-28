plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.deleteAccount)
    implementation(projects.core.model)
    implementation(projects.core.provider.billing)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
}
