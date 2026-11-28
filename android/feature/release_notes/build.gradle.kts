plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.releaseNotes)
    implementation(projects.core.model)
    implementation(projects.core.date)
    implementation(projects.ui.utils)
}
