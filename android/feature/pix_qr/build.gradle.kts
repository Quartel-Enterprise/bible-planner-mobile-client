plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.donation.pixQr)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
}
