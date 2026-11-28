plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.preferences.bibleVersion)
    implementation(projects.core.books)
    implementation(projects.core.model)
    implementation(projects.core.utils)
    implementation(libs.compose.materialIconsExtended)
}
