plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.day)
    // Why: the day study card keeps its Compose Multiplatform version until the study screens move to native.
    implementation(projects.feature.dayStudy)
    implementation(projects.core.model)
    implementation(projects.core.books)
    implementation(projects.core.plan)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.kotlinx.datetime)
}
