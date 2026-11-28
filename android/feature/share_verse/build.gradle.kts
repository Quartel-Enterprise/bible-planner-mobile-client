plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.verse.share)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.image)
    implementation(projects.core.utils)
    implementation(projects.ui.component)
    implementation(projects.ui.theme)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.filekit.dialogs.compose)
}
