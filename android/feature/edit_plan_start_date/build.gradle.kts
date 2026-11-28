plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.preferences.editPlanStartDate)
    implementation(projects.core.model)
    implementation(projects.ui.utils)
}
