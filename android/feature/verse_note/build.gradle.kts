plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.verse.addNote)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.ui.theme)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    // Book names stay in the shared Compose resources instead of being duplicated.
    implementation(libs.compose.components.resources)
}
