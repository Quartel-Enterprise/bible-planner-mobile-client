package com.quare.bibleplanner.e2e

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.core.preferences.themeselection.domain.usecase.SetThemeOption
import com.quare.bibleplanner.e2e.harness.E2eApp
import com.quare.bibleplanner.e2e.harness.E2eWindow
import com.quare.bibleplanner.e2e.harness.awaitDarkScreen
import com.quare.bibleplanner.e2e.harness.awaitLightScreen
import com.quare.bibleplanner.e2e.harness.awaitNode
import com.quare.bibleplanner.e2e.harness.awaitText
import com.quare.bibleplanner.e2e.harness.clickDescription
import com.quare.bibleplanner.e2e.harness.clickText
import org.koin.core.Koin
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class PreferencesFlowUiTest {
    private val app = E2eApp()

    @AfterTest
    fun tearDown() {
        app.stop()
    }

    @Test
    fun `GIVEN the light theme WHEN choosing the dark theme THEN darkens the screen`() = runComposeUiTest {
        // Given
        prepareScenario(
            window = E2eWindow.PORTRAIT,
            arrange = { get<SetThemeOption>()(Theme.LIGHT) },
        )
        awaitLightScreen()

        // When
        chooseTheDarkTheme()

        // Then
        awaitDarkScreen()
    }

    @Test
    fun `GIVEN the light theme in a wide window WHEN choosing the dark theme THEN darkens the screen`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.WIDE,
                arrange = { get<SetThemeOption>()(Theme.LIGHT) },
            )
            awaitLightScreen()

            // When
            chooseTheDarkTheme()

            // Then
            awaitDarkScreen()
        }

    @Test
    fun `GIVEN the app in English WHEN choosing Portuguese THEN shows the screens in Portuguese`() = runComposeUiTest {
        // Given
        prepareScenario(
            window = E2eWindow.PORTRAIT,
            arrange = {},
        )

        // When
        chooseBrazilianPortuguese()

        // Then
        assertTheAppIsInPortuguese()
    }

    @Test
    fun `GIVEN a wide window in English WHEN choosing Portuguese THEN shows the screens in Portuguese`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.WIDE,
                arrange = {},
            )

            // When
            chooseBrazilianPortuguese()

            // Then
            assertTheAppIsInPortuguese()
        }

    @Test
    fun `GIVEN the default Bible version WHEN downloading and choosing another THEN shows the chapters in it`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.PORTRAIT,
                arrange = {},
            )

            // When
            downloadAndChooseTheKingJamesVersion()

            // Then
            readTheFirstChapterOfGenesis()
            awaitText("KJV Gn 1:1")
        }

    @Test
    fun `GIVEN a wide window WHEN downloading and choosing another Bible version THEN shows the chapters in it`() =
        runComposeUiTest {
            // Given
            prepareScenario(
                window = E2eWindow.WIDE,
                arrange = {},
            )

            // When
            downloadAndChooseTheKingJamesVersion()

            // Then
            readTheFirstChapterOfGenesis()
            awaitText("KJV Gn 1:1")
        }

    private fun ComposeUiTest.chooseTheDarkTheme() {
        clickText("Profile")
        clickText("Theme")
        clickText("Dark")
        clickDescription("Close")
        awaitText("Dark • Standard Contrast")
    }

    private fun ComposeUiTest.chooseBrazilianPortuguese() {
        clickText("Profile")
        clickText("Language")
        clickText("Portuguese (Brazil)")
    }

    private fun ComposeUiTest.assertTheAppIsInPortuguese() {
        awaitText("Idioma")
        awaitText("Português (Brasil)")
        awaitText("Perfil")
    }

    private fun ComposeUiTest.downloadAndChooseTheKingJamesVersion() {
        clickText("Profile")
        clickText("Bible version")
        clickDescription("Download")
        awaitNode(hasText("King James Version") and hasText("Downloaded"))
        clickText("King James Version")
        clickDescription("Close")
        awaitText("King James Version")
    }

    private fun ComposeUiTest.readTheFirstChapterOfGenesis() {
        clickText("Books")
        clickText("Genesis")
        clickText("1")
    }

    private suspend fun ComposeUiTest.prepareScenario(
        window: E2eWindow,
        arrange: suspend Koin.() -> Unit,
    ) {
        with(app) {
            launch(
                window = window,
                arrange = arrange,
            )
        }
    }
}
