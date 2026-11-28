plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.contactSupport)
    implementation(projects.core.model)
    implementation(projects.core.provider.billing)
    implementation(projects.core.provider.platform)
    implementation(projects.core.user)
    implementation(projects.core.utils)
    implementation(projects.ui.utils)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.quare.loadable)
}
