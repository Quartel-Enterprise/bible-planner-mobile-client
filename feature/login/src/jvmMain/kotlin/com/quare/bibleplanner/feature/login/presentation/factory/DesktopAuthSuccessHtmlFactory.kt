package com.quare.bibleplanner.feature.login.presentation.factory

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.GetResourcesAsTextResult
import com.quare.bibleplanner.feature.login.presentation.mapper.LanguageToDesktopAuthSuccessStringsMapper
import com.quare.bibleplanner.ui.theme.model.Theme

/**
 * Builds the HTML shown in the browser after a successful desktop OAuth login.
 *
 * The template, both theme stylesheets, and the script are bundled under
 * `jvmMain/resources/com/quare/bibleplanner/feature/login/auth/`. All resource reads go
 * through [GetResourcesAsTextResult] so a missing or unreadable file is surfaced as
 * [Result.failure] rather than a thrown exception.
 *
 * Layout / typography / animation live in a single `_base.css` shared by both themes.
 * Each theme stylesheet only declares the color overrides.
 *
 * - When `theme` is [Theme.SYSTEM] both theme stylesheets are inlined inside
 *   `@media (prefers-color-scheme: …)` blocks so the browser picks the right palette.
 * - When the user pinned [Theme.LIGHT] or [Theme.DARK] explicitly, only that stylesheet
 *   is inlined unconditionally.
 *
 * `language` selects the heading/message strings via the injected mapper.
 */
internal class DesktopAuthSuccessHtmlFactory(
    private val getResourcesAsTextResult: GetResourcesAsTextResult,
    private val languageToDesktopAuthSuccessStringsMapper: LanguageToDesktopAuthSuccessStringsMapper,
) {
    fun create(
        theme: Theme,
        language: Language,
    ): Result<String> = runCatching {
        val template = loadOrThrow("$AUTH_RESOURCE_PREFIX/desktop_auth_success.html")
        val script = loadOrThrow("$AUTH_RESOURCE_PREFIX/desktop_auth_success.js")
        val baseCss = loadOrThrow("$AUTH_RESOURCE_PREFIX/desktop_auth_success_base.css")
        val lightCss by lazy { loadOrThrow("$AUTH_RESOURCE_PREFIX/desktop_auth_success_light.css") }
        val darkCss by lazy { loadOrThrow("$AUTH_RESOURCE_PREFIX/desktop_auth_success_dark.css") }

        val themeCss = when (theme) {
            Theme.LIGHT -> lightCss

            Theme.DARK -> darkCss

            Theme.SYSTEM -> buildString {
                appendLine("@media (prefers-color-scheme: light) {")
                appendLine(lightCss)
                appendLine("}")
                appendLine("@media (prefers-color-scheme: dark) {")
                appendLine(darkCss)
                appendLine("}")
            }
        }
        // Base FIRST so the theme rules win in the cascade.
        val styles = "$baseCss\n$themeCss"

        val strings = languageToDesktopAuthSuccessStringsMapper.map(language)
        template
            .replace("{{TITLE}}", "Bible Planner")
            .replace("{{HEADING}}", strings.heading)
            .replace("{{MESSAGE}}", strings.message)
            .replace("{{LANG}}", strings.htmlLang)
            .replace("{{STYLES}}", styles)
            .replace("{{SCRIPT}}", script)
    }

    private fun loadOrThrow(path: String): String = getResourcesAsTextResult(path).getOrThrow()

    private companion object {
        const val AUTH_RESOURCE_PREFIX: String = "com/quare/bibleplanner/feature/login/auth"
    }
}
