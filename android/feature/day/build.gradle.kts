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
    implementation(projects.core.utils)
    implementation(projects.ui.utils)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.kotlinx.datetime)
    // Why: the month name stays in the shared Compose resources, as on the plans tab it flies from.
    implementation(libs.compose.components.resources)
}
