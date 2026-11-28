plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.verse.selectionMenu)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.verseAnnotations)
    implementation(projects.ui.component)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
}
