plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.dayReadingComplete)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.utils)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.shimmer)
    implementation(libs.kotlinx.datetime)
}
