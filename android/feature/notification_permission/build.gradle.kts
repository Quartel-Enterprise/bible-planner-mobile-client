plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.notificationPermission)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
    implementation(libs.androidx.activity.compose)
}
