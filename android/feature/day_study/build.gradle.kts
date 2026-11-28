plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.dayStudy)
    implementation(projects.core.model)
    implementation(projects.core.dayStudy)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.quare.loadable)
}
