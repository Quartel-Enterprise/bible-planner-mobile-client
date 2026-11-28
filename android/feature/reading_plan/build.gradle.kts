plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.readingPlan)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.utils)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.kotlinx.datetime)
    implementation(libs.compose.shimmer)
    // Book and month names stay in the shared Compose resources instead of being duplicated.
    implementation(libs.compose.components.resources)
}
