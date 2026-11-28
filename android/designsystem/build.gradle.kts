plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    // Why: AppModalBottomSheet hands its content a SheetCloseGuard, so every screen using it needs that type.
    api(projects.ui.utils)
    implementation(projects.core.utils)
}
