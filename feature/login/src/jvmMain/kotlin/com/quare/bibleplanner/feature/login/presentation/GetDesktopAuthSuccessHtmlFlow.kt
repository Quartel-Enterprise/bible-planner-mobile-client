package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.GetThemeOptionFlow
import com.quare.bibleplanner.core.provider.language.domain.usecase.GetAppLanguageFlow
import com.quare.bibleplanner.feature.login.presentation.factory.DesktopAuthSuccessHtmlFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

// Why: rendering errors (e.g. a missing classpath resource) are emitted as Result.failure so the
// synchronizer reports them instead of crashing the OAuth flow.
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
