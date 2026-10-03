package com.quare.bibleplanner.feature.login.presentation

import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.feature.login.presentation.factory.DesktopAuthSuccessHtmlFactory
import com.quare.bibleplanner.feature.login.presentation.mapper.LanguageToDesktopAuthSuccessStringsMapper
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import java.io.InputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DesktopAuthRedirectHtmlSynchronizerTest {
    private lateinit var synchronizer: DesktopAuthRedirectHtmlSynchronizer
    private lateinit var auth: Auth
    private lateinit var language: MutableStateFlow<Language>
    private lateinit var resourcesClassLoader: SwitchableResourcesClassLoader
    private var originalContextClassLoader: ClassLoader? = null

    @AfterTest
    fun tearDown() {
        Thread.currentThread().contextClassLoader = originalContextClassLoader
    }

    @Test
    fun `GIVEN the page renders WHEN running the sign in block THEN installs the rendered page before the block`() =
        runTest {
            // Given
            prepareScenario(isRenderFailing = false)
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

    @Test
    fun `GIVEN the first render fails WHEN running the sign in block THEN reports it and skips the block`() = runTest {
        // Given
        prepareScenario(isRenderFailing = true)
        var hasBlockRun = false
        val errors = mutableListOf<Throwable>()

        // When
        synchronizer.withSyncedRedirectHtml(onError = { errors += it }) {
            hasBlockRun = true
        }

        // Then
        assertFalse(hasBlockRun)
        assertEquals(
            expected = listOf("Resource not found on classpath: $TEMPLATE_PATH"),
            actual = errors.map { it.message },
        )
    }

    @Test
    fun `GIVEN a later render fails WHEN running the sign in block THEN reports it and completes the block`() =
        runTest {
            // Given
            prepareScenario(isRenderFailing = false)
            var hasBlockCompleted = false
            var redirectHtmlAfterFailure: String? = null
            val errors = mutableListOf<Throwable>()

            // When
            synchronizer.withSyncedRedirectHtml(onError = { errors += it }) {
                resourcesClassLoader.isFailing = true
                language.value = Language.ENGLISH
                delay(1)
                redirectHtmlAfterFailure = auth.config.httpCallbackConfig.redirectHtml
                hasBlockCompleted = true
            }

            // Then
            assertTrue(hasBlockCompleted)
            assertEquals(
                expected = listOf("Resource not found on classpath: $TEMPLATE_PATH"),
                actual = errors.map { it.message },
            )
            assertTrue(redirectHtmlAfterFailure.orEmpty().contains("Sesión iniciada"))
        }

    private fun prepareScenario(isRenderFailing: Boolean) {
        originalContextClassLoader = Thread.currentThread().contextClassLoader
        resourcesClassLoader = SwitchableResourcesClassLoader(parent = javaClass.classLoader)
        resourcesClassLoader.isFailing = isRenderFailing
        Thread.currentThread().contextClassLoader = resourcesClassLoader
        language = MutableStateFlow(Language.SPANISH)
        auth = createTestSupabaseClient().auth
        synchronizer = DesktopAuthRedirectHtmlSynchronizer(
            auth = auth,
            getDesktopAuthSuccessHtmlFlow = GetDesktopAuthSuccessHtmlFlow(
                getThemeOptionFlow = { MutableStateFlow(Theme.SYSTEM) },
                getAppLanguageFlow = { language },
                desktopAuthSuccessHtmlFactory = DesktopAuthSuccessHtmlFactory(
                    getResourcesAsTextResult = GetResourcesAsTextResult(),
                    languageToDesktopAuthSuccessStringsMapper = LanguageToDesktopAuthSuccessStringsMapper(),
                ),
            ),
        )
    }

    private companion object {
        const val TEMPLATE_PATH: String = "com/quare/bibleplanner/feature/login/auth/desktop_auth_success.html"
    }
}

private class SwitchableResourcesClassLoader(
    parent: ClassLoader?,
) : ClassLoader(parent) {
    var isFailing: Boolean = false

    override fun getResourceAsStream(name: String): InputStream? = if (isFailing) {
        null
    } else {
        super.getResourceAsStream(name)
    }
}
