plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.logout)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
}
