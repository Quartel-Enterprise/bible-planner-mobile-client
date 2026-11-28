plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.paywall)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.androidx.lifecycle.runtimeCompose)
}
