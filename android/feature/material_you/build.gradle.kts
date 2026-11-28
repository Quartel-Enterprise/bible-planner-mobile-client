plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.materialYou)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
}
dependencies {
    implementation(libs.compose.materialIconsExtended)
}
