plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.books)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    // Book group and testament titles stay in the shared Compose resources instead of being duplicated.
    implementation(libs.compose.components.resources)
}
