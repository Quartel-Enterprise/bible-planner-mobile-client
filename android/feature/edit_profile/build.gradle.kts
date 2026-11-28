plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.editProfile)
    implementation(projects.core.model)
    implementation(projects.core.profile)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.filekit.dialogs.compose)
}
