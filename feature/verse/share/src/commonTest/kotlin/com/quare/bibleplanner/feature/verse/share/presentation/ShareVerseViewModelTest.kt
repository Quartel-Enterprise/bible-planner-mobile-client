package com.quare.bibleplanner.feature.verse.share.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.colorspace.ColorSpace
import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_photo_load_failed
import com.quare.bibleplanner.core.books.domain.model.VersesShareContentModel
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserReason
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.plan.domain.usecase.GetReadingStreak
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackground
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardBackgroundUiModel
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareCardCustomColor
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiAction
import com.quare.bibleplanner.feature.verse.share.presentation.model.ShareVerseUiEvent
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ShareVerseViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val verseNumbers = listOf(1, 2)
    private val pickedPhoto: ImageBitmap = FakeImageBitmap()
    private val shareImageStylePaywall: NavigationCommand =
        NavigationCommand.Navigate(PaywallTeaserNavRoute(PaywallTeaserReason.SHARE_IMAGE_STYLE))
    private lateinit var viewModel: ShareVerseViewModel
    private lateinit var actions: List<ShareVerseUiAction>
    private val navigator = Navigator()
    private lateinit var commands: List<NavigationCommand>
    private lateinit var trackedEvents: MutableList<Pair<String, Map<String, Any>>>
    private lateinit var isProUser: MutableStateFlow<Boolean>
    private lateinit var decodedFiles: MutableList<PlatformFile>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a passage WHEN opening the share sheet THEN loads it into the card`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        runCurrent()

        // Then
        assertEquals(
            expected = "1 Primeiro 2 Segundo",
            actual = viewModel.uiState.value.quote,
        )
        assertTrue(viewModel.uiState.value.isReady)
    }

    @Test
    fun `GIVEN a loaded passage WHEN sharing as text THEN emits the formatted message`() = runTest(testDispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnShareAsTextClick)
        runCurrent()

        // Then
        val action = actions.filterIsInstance<ShareVerseUiAction.ShareText>().single()
        assertEquals(
            expected = "1 Primeiro 2 Segundo",
            actual = action.content.text,
        )
        assertEquals(
            expected = "Gênesis 3:1-2",
            actual = action.content.reference,
        )
        assertEquals(
            expected = "text",
            actual = trackedEvents.single { it.first == "verse_shared" }.second["format"],
        )
    }

    @Test
    fun `GIVEN a loaded passage WHEN sharing as an image THEN opens the composer for the same passage`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnShareAsImageClick)
            runCurrent()

            // Then
            assertEquals(
                expected = NavigationCommand.Navigate(
                    ShareVerseImageNavRoute(
                        bookId = BookId.GEN.name,
                        chapterNumber = 3,
                        verseNumbers = verseNumbers,
                    ),
                ),
                actual = commands.single(),
            )
        }

    @Test
    fun `GIVEN a loaded passage WHEN sharing the rendered card THEN reports the image format`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnShareImageReady(byteArrayOf(1, 2, 3)))
            runCurrent()

            // Then
            val action = actions.filterIsInstance<ShareVerseUiAction.ShareImage>().single()
            assertTrue(action.imageBytes.contentEquals(byteArrayOf(1, 2, 3)))
            assertEquals(
                expected = "image",
                actual = trackedEvents.single { it.first == "verse_shared" }.second["format"],
            )
        }

    @Test
    fun `GIVEN a loaded passage WHEN choosing a background THEN applies it and tracks it in the style event`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnBackgroundClick(ShareCardBackground.FOREST))
            runCurrent()

            // Then
            assertEquals(
                expected = ShareCardBackgroundUiModel.Preset(ShareCardBackground.FOREST),
                actual = viewModel.uiState.value.background,
            )
            assertEquals(
                expected = "forest",
                actual = trackedEvents.single { it.first == "verse_share_style_changed" }.second["background"],
            )
        }

    @Test
    fun `GIVEN a 12 day streak WHEN opening the composer THEN offers it with the badge off and the mark on`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(readingStreak = { 12 })

            // When
            runCurrent()

            // Then
            val state = viewModel.uiState.value
            assertEquals(
                expected = 12,
                actual = state.streakDays,
            )
            assertFalse(state.isStreakShown)
            assertTrue(state.isWatermarkShown)
        }

    @Test
    fun `GIVEN the streak lookup fails WHEN opening the composer THEN leaves the streak at zero`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(readingStreak = { error("no plan") })

            // When
            runCurrent()

            // Then
            assertEquals(
                expected = 0,
                actual = viewModel.uiState.value.streakDays,
            )
            assertTrue(viewModel.uiState.value.isReady)
        }

    @Test
    fun `GIVEN a free user with a 12 day streak WHEN adding the streak THEN shows the badge and tracks it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(readingStreak = { 12 })

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnStreakToggleClick)
            runCurrent()

            // Then
            assertTrue(viewModel.uiState.value.isStreakShown)
            assertEquals(
                expected = mapOf<String, Any>(
                    "is_enabled" to true,
                    "streak_days" to 12,
                ),
                actual = trackedEvents.single { it.first == "verse_share_streak_toggled" }.second,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN the streak badge is shown WHEN removing the streak THEN hides the badge and tracks it as disabled`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(readingStreak = { 12 })
            viewModel.onEvent(ShareVerseUiEvent.OnStreakToggleClick)
            trackedEvents.clear()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnStreakToggleClick)
            runCurrent()

            // Then
            assertFalse(viewModel.uiState.value.isStreakShown)
            assertEquals(
                expected = mapOf<String, Any>(
                    "is_enabled" to false,
                    "streak_days" to 12,
                ),
                actual = trackedEvents.single { it.first == "verse_share_streak_toggled" }.second,
            )
        }

    @Test
    fun `GIVEN a Pro user WHEN removing the mark THEN hides it and tracks the toggle`() = runTest(testDispatcher) {
        // Given
        prepareScenario(isPro = true)

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnWatermarkToggleClick)
        runCurrent()

        // Then
        assertFalse(viewModel.uiState.value.isWatermarkShown)
        assertEquals(
            expected = mapOf<String, Any>("is_enabled" to false),
            actual = trackedEvents.single { it.first == "verse_share_watermark_toggled" }.second,
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `GIVEN a Pro user without the mark WHEN showing the mark THEN puts it back and tracks it as enabled`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(ShareVerseUiEvent.OnWatermarkToggleClick)
            trackedEvents.clear()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnWatermarkToggleClick)
            runCurrent()

            // Then
            assertTrue(viewModel.uiState.value.isWatermarkShown)
            assertEquals(
                expected = mapOf<String, Any>("is_enabled" to true),
                actual = trackedEvents.single { it.first == "verse_share_watermark_toggled" }.second,
            )
        }

    @Test
    fun `GIVEN a free user WHEN removing the mark THEN keeps it and opens the paywall teaser`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnWatermarkToggleClick)
            runCurrent()

            // Then
            assertTrue(viewModel.uiState.value.isWatermarkShown)
            assertEquals(
                expected = shareImageStylePaywall,
                actual = commands.single(),
            )
            assertEquals(
                expected = mapOf<String, Any>("option" to "watermark"),
                actual = trackedEvents.single { it.first == "verse_share_pro_option_locked_clicked" }.second,
            )
            assertTrue(trackedEvents.none { it.first == "verse_share_watermark_toggled" })
        }

    @Test
    fun `GIVEN a Pro user on a preset WHEN tapping the custom color THEN switches to it and opens the picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            val customColor = viewModel.uiState.value.customColor

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            runCurrent()

            // Then
            assertEquals(
                expected = ShareCardBackgroundUiModel.Custom(customColor),
                actual = viewModel.uiState.value.background,
            )
            assertTrue(viewModel.uiState.value.isColorPickerOpen)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a Pro user on a preset WHEN tapping the custom color THEN tracks the picker and custom style`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            runCurrent()

            // Then
            assertEquals(
                expected = mapOf<String, Any>("is_open" to true),
                actual = trackedEvents.single { it.first == "verse_share_color_picker_toggled" }.second,
            )
            assertEquals(
                expected = "custom",
                actual = trackedEvents.single { it.first == "verse_share_style_changed" }.second["background"],
            )
        }

    @Test
    fun `GIVEN the picker open WHEN tapping the custom color again THEN closes it without a style event`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            val customBackground = viewModel.uiState.value.background
            trackedEvents.clear()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            runCurrent()

            // Then
            assertFalse(viewModel.uiState.value.isColorPickerOpen)
            assertEquals(
                expected = customBackground,
                actual = viewModel.uiState.value.background,
            )
            assertEquals(
                expected = mapOf<String, Any>("is_open" to false),
                actual = trackedEvents.single { it.first == "verse_share_color_picker_toggled" }.second,
            )
            assertTrue(trackedEvents.none { it.first == "verse_share_style_changed" })
        }

    @Test
    fun `GIVEN the picker open WHEN moving the sliders THEN updates the custom background without tracking`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            trackedEvents.clear()

            // When
            viewModel.onEvent(
                ShareVerseUiEvent.OnCustomColorChange(
                    hue = 120,
                    lightness = 70,
                ),
            )
            runCurrent()

            // Then
            val expectedColor = ShareCardCustomColor(
                hue = 120,
                lightness = 70,
            )
            assertEquals(
                expected = expectedColor,
                actual = viewModel.uiState.value.customColor,
            )
            assertEquals(
                expected = ShareCardBackgroundUiModel.Custom(expectedColor),
                actual = viewModel.uiState.value.background,
            )
            assertTrue(viewModel.uiState.value.isColorPickerOpen)
            assertTrue(trackedEvents.isEmpty())
        }

    @Test
    fun `GIVEN a picked custom color and a preset WHEN tapping the custom color THEN restores that color`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            viewModel.onEvent(
                ShareVerseUiEvent.OnCustomColorChange(
                    hue = 30,
                    lightness = 55,
                ),
            )
            viewModel.onEvent(ShareVerseUiEvent.OnBackgroundClick(ShareCardBackground.SKY))

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            runCurrent()

            // Then
            assertEquals(
                expected = ShareCardBackgroundUiModel.Custom(
                    ShareCardCustomColor(
                        hue = 30,
                        lightness = 55,
                    ),
                ),
                actual = viewModel.uiState.value.background,
            )
            assertTrue(viewModel.uiState.value.isColorPickerOpen)
        }

    @Test
    fun `GIVEN a Pro user with the picker open WHEN choosing a preset THEN closes the picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnBackgroundClick(ShareCardBackground.WINE))
            runCurrent()

            // Then
            assertFalse(viewModel.uiState.value.isColorPickerOpen)
            assertEquals(
                expected = ShareCardBackgroundUiModel.Preset(ShareCardBackground.WINE),
                actual = viewModel.uiState.value.background,
            )
        }

    @Test
    fun `GIVEN a free user WHEN tapping the custom color THEN keeps the card and opens the paywall teaser`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)
            val stateBefore = viewModel.uiState.value

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            runCurrent()

            // Then
            assertEquals(
                expected = stateBefore,
                actual = viewModel.uiState.value,
            )
            assertEquals(
                expected = shareImageStylePaywall,
                actual = commands.single(),
            )
            assertEquals(
                expected = mapOf<String, Any>("option" to "custom_color"),
                actual = trackedEvents.single { it.first == "verse_share_pro_option_locked_clicked" }.second,
            )
            assertTrue(trackedEvents.none { it.first == "verse_share_color_picker_toggled" })
            assertTrue(trackedEvents.none { it.first == "verse_share_style_changed" })
        }

    @Test
    fun `GIVEN a Pro user WHEN tapping the photo background THEN launches the photo picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoBackgroundClick)
            runCurrent()

            // Then
            assertEquals(
                expected = listOf<ShareVerseUiAction>(ShareVerseUiAction.LaunchPhotoPicker),
                actual = actions,
            )
            assertTrue(commands.isEmpty())
            assertTrue(trackedEvents.none { it.first == "verse_share_pro_option_locked_clicked" })
        }

    @Test
    fun `GIVEN a free user WHEN tapping the photo background THEN opens the paywall teaser instead of the picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoBackgroundClick)
            runCurrent()

            // Then
            assertTrue(actions.isEmpty())
            assertEquals(
                expected = shareImageStylePaywall,
                actual = commands.single(),
            )
            assertEquals(
                expected = mapOf<String, Any>("option" to "photo"),
                actual = trackedEvents.single { it.first == "verse_share_pro_option_locked_clicked" }.second,
            )
        }

    @Test
    fun `GIVEN the color picker open WHEN a photo is picked THEN uses it as background and closes the picker`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isPro = true,
                decodeResult = Result.success(pickedPhoto),
            )
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            val file = PlatformFile("photo.jpg")

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoPicked(file))
            runCurrent()

            // Then
            assertEquals(
                expected = ShareCardBackgroundUiModel.Photo(pickedPhoto),
                actual = viewModel.uiState.value.background,
            )
            assertFalse(viewModel.uiState.value.isColorPickerOpen)
            assertEquals(
                expected = listOf(file),
                actual = decodedFiles,
            )
        }

    @Test
    fun `GIVEN a Pro user WHEN a photo is picked THEN tracks the photo style`() = runTest(testDispatcher) {
        // Given
        prepareScenario(
            isPro = true,
            decodeResult = Result.success(pickedPhoto),
        )

        // When
        viewModel.onEvent(ShareVerseUiEvent.OnPhotoPicked(PlatformFile("photo.jpg")))
        runCurrent()

        // Then
        assertEquals(
            expected = "photo",
            actual = trackedEvents.single { it.first == "verse_share_style_changed" }.second["background"],
        )
    }

    @Test
    fun `GIVEN a Pro user WHEN the photo picker is cancelled THEN keeps the card and tracks nothing`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isPro = true,
                decodeResult = Result.success(pickedPhoto),
            )
            val stateBefore = viewModel.uiState.value

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoPicked(null))
            runCurrent()

            // Then
            assertEquals(
                expected = stateBefore,
                actual = viewModel.uiState.value,
            )
            assertTrue(decodedFiles.isEmpty())
            assertTrue(actions.isEmpty())
            assertTrue(trackedEvents.isEmpty())
        }

    @Test
    fun `GIVEN a photo that fails to decode WHEN it is picked THEN shows the load failure and keeps the background`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isPro = true,
                decodeResult = Result.failure(IllegalStateException("corrupt")),
            )
            viewModel.onEvent(ShareVerseUiEvent.OnBackgroundClick(ShareCardBackground.MIDNIGHT))
            trackedEvents.clear()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoPicked(PlatformFile("broken.jpg")))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf<ShareVerseUiAction>(
                    ShareVerseUiAction.ShowMessage(Res.string.share_photo_load_failed),
                ),
                actual = actions,
            )
            assertEquals(
                expected = ShareCardBackgroundUiModel.Preset(ShareCardBackground.MIDNIGHT),
                actual = viewModel.uiState.value.background,
            )
            assertTrue(trackedEvents.none { it.first == "verse_share_style_changed" })
        }

    @Test
    fun `GIVEN a free user on the composer WHEN subscribing to Pro THEN the state reports Pro`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)

            // When
            isProUser.value = true
            runCurrent()

            // Then
            assertTrue(viewModel.uiState.value.isProUser)
        }

    @Test
    fun `GIVEN a user who just subscribed WHEN removing the mark THEN hides it without the paywall`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)
            isProUser.value = true
            runCurrent()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnWatermarkToggleClick)
            runCurrent()

            // Then
            assertFalse(viewModel.uiState.value.isWatermarkShown)
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN a user whose Pro just ended WHEN tapping the photo background THEN opens the paywall teaser`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            isProUser.value = false
            runCurrent()

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoBackgroundClick)
            runCurrent()

            // Then
            assertTrue(actions.isEmpty())
            assertEquals(
                expected = shareImageStylePaywall,
                actual = commands.single(),
            )
        }

    @Test
    fun `GIVEN a rendered card WHEN downloading it THEN asks to save the same bytes under a passage file name`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario()
            val imageBytes = byteArrayOf(4, 5, 6)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnDownloadImageReady(imageBytes))
            runCurrent()

            // Then
            assertEquals(
                expected = listOf<ShareVerseUiAction>(
                    ShareVerseUiAction.SaveImage(
                        imageBytes = byteArrayOf(4, 5, 6),
                        fileName = "bible_planner_gen_3_1-2.png",
                    ),
                ),
                actual = actions,
            )
        }

    @Test
    fun `GIVEN a free user with a rendered card WHEN downloading it THEN tracks the download with the verse count`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = false)

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnDownloadImageReady(byteArrayOf(4, 5, 6)))
            runCurrent()

            // Then
            assertEquals(
                expected = mapOf<String, Any>("verse_count" to 2),
                actual = trackedEvents.single { it.first == "verse_share_image_downloaded" }.second,
            )
            assertTrue(commands.isEmpty())
        }

    @Test
    fun `GIVEN Pro expired with the picker open WHEN moving the sliders THEN keeps the card unchanged`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(isPro = true)
            viewModel.onEvent(ShareVerseUiEvent.OnCustomColorClick)
            isProUser.value = false
            val stateBefore = viewModel.uiState.value.copy(isProUser = false)

            // When
            viewModel.onEvent(
                ShareVerseUiEvent.OnCustomColorChange(
                    hue = 120,
                    lightness = 70,
                ),
            )
            runCurrent()

            // Then
            assertEquals(
                expected = stateBefore,
                actual = viewModel.uiState.value,
            )
        }

    @Test
    fun `GIVEN Pro expired while picking a photo WHEN it is picked THEN neither decodes nor applies it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(
                isPro = false,
                decodeResult = Result.success(pickedPhoto),
            )

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnPhotoPicked(PlatformFile("photo.jpg")))
            runCurrent()

            // Then
            assertTrue(decodedFiles.isEmpty())
            assertEquals(
                expected = ShareCardBackgroundUiModel.Preset(ShareCardBackground.VIOLET),
                actual = viewModel.uiState.value.background,
            )
        }

    @Test
    fun `GIVEN a long passage WHEN downloading it THEN names the file after its first and last verse`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(verseNumbers = (1..40).toList())

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnDownloadImageReady(byteArrayOf(4, 5, 6)))
            runCurrent()

            // Then
            assertEquals(
                expected = "bible_planner_gen_3_1-40.png",
                actual = actions.filterIsInstance<ShareVerseUiAction.SaveImage>().single().fileName,
            )
        }

    @Test
    fun `GIVEN a single verse WHEN downloading it THEN names the file after that verse only`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(verseNumbers = listOf(7))

            // When
            viewModel.onEvent(ShareVerseUiEvent.OnDownloadImageReady(byteArrayOf(4, 5, 6)))
            runCurrent()

            // Then
            assertEquals(
                expected = "bible_planner_gen_3_7.png",
                actual = actions.filterIsInstance<ShareVerseUiAction.SaveImage>().single().fileName,
            )
        }

    private fun TestScope.prepareScenario(
        isPro: Boolean = false,
        readingStreak: GetReadingStreak = GetReadingStreak { 0 },
        decodeResult: Result<ImageBitmap> = Result.failure(IllegalStateException("unused")),
        verseNumbers: List<Int> = this@ShareVerseViewModelTest.verseNumbers,
    ) {
        trackedEvents = mutableListOf()
        isProUser = MutableStateFlow(isPro)
        decodedFiles = mutableListOf()
        viewModel = ShareVerseViewModel(
            route = ShareVerseNavRoute(
                bookId = BookId.GEN.name,
                chapterNumber = 3,
                verseNumbers = verseNumbers,
            ),
            getVersesShareContent = { _, _, _ ->
                VersesShareContentModel(
                    text = "1 Primeiro 2 Segundo",
                    reference = "Gênesis 3:1-2",
                    versionAbbreviation = "ARC",
                )
            },
            getReadingStreak = readingStreak,
            decodeImageBitmap = { file ->
                decodedFiles += file
                decodeResult
            },
            platform = Platform.Android,
            navigator = navigator,
            observeIsProUser = { isProUser },
            trackEvent = { name, params -> trackedEvents += name to params },
        )
        actions = mutableListOf<ShareVerseUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
        commands = mutableListOf<NavigationCommand>().also { collected ->
            backgroundScope.launch { navigator.commands.collect { collected += it } }
        }
    }
}

private class FakeImageBitmap : ImageBitmap {
    override val width: Int = 1
    override val height: Int = 1
    override val colorSpace: ColorSpace get() = error("unused")
    override val hasAlpha: Boolean get() = error("unused")
    override val config: ImageBitmapConfig get() = error("unused")

    override fun readPixels(
        buffer: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        bufferOffset: Int,
        stride: Int,
    ) = error("unused")

    override fun prepareToDraw() = error("unused")
}
