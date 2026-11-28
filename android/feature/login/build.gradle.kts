plugins {
    alias(libs.plugins.bibleplanner.android.compose)
}

dependencies {
    implementation(projects.feature.login)
    implementation(projects.core.model)
    implementation(projects.ui.utils)

    // Supabase compose-auth: the native Google and Apple sign-in states the shared ViewModel starts.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.compose.auth)
}
