plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.preferences.appLanguage)
    implementation(projects.core.model)
    implementation(projects.core.utils)
}
