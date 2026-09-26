package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.factory.DesktopAuthSuccessHtmlFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.LanguageToDesktopAuthSuccessStringsMapper
import com.quare.bibleplanner.core.model.theme.Theme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DesktopAuthRedirectHtmlSynchronizerTest {
    private lateinit var synchronizer: DesktopAuthRedirectHtmlSynchronizer
    private lateinit var auth: Auth

    @Test
    fun `GIVEN the page renders WHEN running the sign in block THEN installs the rendered page before the block`() =
        runTest {
            // Given
            prepareScenario()
            var redirectHtmlSeenByBlock: String? = null
            val errors = mutableListOf<Throwable>()

            // When
            synchronizer.withSyncedRedirectHtml(onError = { errors += it }) {
                redirectHtmlSeenByBlock = auth.config.httpCallbackConfig.redirectHtml
            }

            // Then
            assertTrue(redirectHtmlSeenByBlock.orEmpty().contains("Sesión iniciada"))
            assertEquals(emptyList(), errors)
        }

    private fun prepareScenario() {
        auth = createTestSupabaseClient().auth
        synchronizer = DesktopAuthRedirectHtmlSynchronizer(
            auth = auth,
            getDesktopAuthSuccessHtmlFlow = GetDesktopAuthSuccessHtmlFlow(
                getThemeOptionFlow = { MutableStateFlow(Theme.SYSTEM) },
                getAppLanguageFlow = { MutableStateFlow(Language.SPANISH) },
                desktopAuthSuccessHtmlFactory = DesktopAuthSuccessHtmlFactory(
                    getResourcesAsTextResult = GetResourcesAsTextResult(),
                    languageToDesktopAuthSuccessStringsMapper = LanguageToDesktopAuthSuccessStringsMapper(),
                ),
            ),
        )
    }
}
