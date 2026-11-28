plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.bookDetails)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.ui.utils)
    // Book names, group titles and synopses stay in the shared Compose resources instead of being duplicated.
    implementation(libs.compose.components.resources)
}
