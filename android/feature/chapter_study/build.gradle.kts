plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.chapterStudy)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.chapterStudy)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
}
