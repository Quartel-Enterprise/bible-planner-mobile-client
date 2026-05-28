package com.quare.bibleplanner.feature.login.domain.usecase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

/**
 * Mutates [io.github.jan.supabase.auth.AuthConfig.httpCallbackConfig].`redirectHtml`
 * via reflection so the next desktop OAuth callback serves the supplied HTML.
 *
 * Reflection is the only no-fork option in supabase-kt 3.6.0 — the field is `internal`
 * and there is no per-call HTML hook in `signInWith`. The desktop callback route reads
 * the field fresh on every request, so calling this between sign-in attempts (or even
 * mid-flow) is safe. If a future supabase-kt release exposes a public accessor or a
 * per-call HTML hook, swap the body of [invoke] for one that uses the public API.
 */
internal class ApplySupabaseRedirectHtmlUseCase(
    private val supabaseClient: SupabaseClient,
) {
    operator fun invoke(html: String) {
        val authConfig = supabaseClient.auth.config
        val httpConfigField = authConfig.javaClass
            .getDeclaredField("httpCallbackConfig")
            .apply { isAccessible = true }
        val httpConfig = httpConfigField.get(authConfig)
        httpConfig.javaClass
            .getDeclaredField("redirectHtml")
            .apply { isAccessible = true }
            .set(httpConfig, html)
    }
}
