plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.read)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.chapterListening)
    implementation(projects.core.verseAnnotations)
    implementation(projects.ui.component)
    implementation(projects.ui.theme)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
}
