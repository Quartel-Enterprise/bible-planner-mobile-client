plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.loginWarning)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
}
