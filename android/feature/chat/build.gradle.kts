plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.chat)
    implementation(projects.core.model)
    implementation(projects.core.date)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.shimmer)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kermit)
}
