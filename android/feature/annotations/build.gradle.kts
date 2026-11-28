plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.verse.annotations)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.date)
    implementation(projects.core.verseAnnotations)
    implementation(projects.ui.component)
    implementation(projects.ui.theme)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.shimmer)
    implementation(libs.kotlinx.datetime)
    implementation(libs.quare.loadable)
    // Book names stay in the shared Compose resources instead of being duplicated.
    implementation(libs.compose.components.resources)
}
