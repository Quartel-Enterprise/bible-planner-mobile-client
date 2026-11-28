plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.accountDetails)
    implementation(projects.core.model)
    implementation(projects.core.date)
    implementation(projects.core.devices)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.kotlinx.datetime)
}
