package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.login.presentation.factory.DesktopAuthSuccessHtmlFactory
import com.quare.bibleplanner.feature.themeselection.domain.usecase.GetThemeOptionFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Emits the desktop OAuth success page (wrapped in [Result]) every time the user's theme
 * or language preference changes. A [Result.failure] reflects a rendering problem (e.g.
 * a missing classpath resource) and lets the downstream synchronizer report the error
 * instead of crashing the OAuth flow.
 */
internal class GetDesktopAuthSuccessHtmlFlow(
    private val getThemeOptionFlow: GetThemeOptionFlow,
    private val getAppLanguageFlow: GetAppLanguageFlow,
    private val desktopAuthSuccessHtmlFactory: DesktopAuthSuccessHtmlFactory,
) {
    operator fun invoke(): Flow<Result<String>> = combine(
        getThemeOptionFlow(),
        getAppLanguageFlow(),
    ) { theme, language ->
        desktopAuthSuccessHtmlFactory.create(theme = theme, language = language)
    }
}
