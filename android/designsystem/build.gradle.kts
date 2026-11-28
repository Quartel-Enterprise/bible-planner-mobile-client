plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.core.date)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.datetime)
}
