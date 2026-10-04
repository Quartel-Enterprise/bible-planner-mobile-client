package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.ui.graphics.ImageBitmap
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.fake.FakeImageBitmap
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.ImageResult
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.CropPhotoViewModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CropPhotoViewModelTest {
    private val navigator = Navigator()
    private val commands = mutableListOf<NavigationCommand>()
    private lateinit var savedPhotos: MutableList<ByteArray>
    private val landscapeBitmap: ImageBitmap = FakeImageBitmap(
        width = 200,
        height = 100,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        savedPhotos = mutableListOf()
        commands.clear()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the crop screen WHEN zooming beyond the maximum THEN keeps the zoom within its bounds`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnZoomChanged(zoom = 99f))

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(uiState.zoomRange.endInclusive, uiState.zoom)
    }

    @Test
    fun `GIVEN the crop screen WHEN flipping horizontally THEN mirrors the photo`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnFlipHorizontalClick)

        // Then
        assertTrue(viewModel.uiState.value.orientation.isFlippedHorizontally)
    }

    @Test
    fun `GIVEN an unrotated photo WHEN tapping rotate twice THEN turns it a quarter clockwise on each tap`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnRotateClick)
        viewModel.onEvent(CropPhotoUiEvent.OnRotateClick)

        // Then
        assertEquals(180, viewModel.uiState.value.orientation.rotationDegrees)
    }

    @Test
    fun `GIVEN an image that has not loaded WHEN confirming THEN ignores the confirm`() = runTest {
        // Given
        // The decoder never completes, so no image is ready
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnConfirmClick)
        advanceUntilIdle()

        // Then
        // Nothing is cropped or saved before the image loads
        assertTrue(savedPhotos.isEmpty())
    }

    @Test
    fun `GIVEN the crop screen WHEN cancelling THEN navigates back`() = runTest {
        // When
        val actions = actionsAfter(CropPhotoUiEvent.OnCancelClick)

        // Then
        assertTrue(actions.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    @Test
    fun `GIVEN an image that cannot be decoded WHEN loading it THEN navigates back and warns`() = runTest {
        // Given
        // A decoder that returns null (unreadable image)
        val viewModel = viewModel(decode = { Result.failure(IllegalStateException("boom")) })
        val actions = mutableListOf<CropPhotoUiAction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiAction.collect { actions.add(it) }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            navigator.commands.collect(commands::add)
        }

        // When
        advanceUntilIdle()
        job.cancel()

        // Then
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertTrue(actions.any { it is CropPhotoUiAction.ShowSnackbar })
    }

    @Test
    fun `GIVEN a decodable image WHEN loading it THEN shows the photo`() = runTest {
        // Given
        val viewModel = viewModel(decode = { Result.success(landscapeBitmap) })

        // When
        advanceUntilIdle()

        // Then
        assertEquals(ImageResult.Loaded(landscapeBitmap), viewModel.uiState.value.image)
    }

    @Test
    fun `GIVEN a measured viewport WHEN panning beyond the photo edges THEN keeps the pan inside them`() = runTest {
        // Given
        val viewModel = viewModel(decode = { Result.success(landscapeBitmap) })
        advanceUntilIdle()
        viewModel.onEvent(
            CropPhotoUiEvent.OnViewportMeasured(
                areaWidth = 300f,
                areaHeight = 300f,
                circleDiameter = 100f,
            ),
        )

        // When
        viewModel.onEvent(
            CropPhotoUiEvent.OnTransform(
                panX = 80f,
                panY = 30f,
                zoomChange = 1f,
            ),
        )

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(50f, uiState.offsetX)
        assertEquals(0f, uiState.offsetY)
    }

    @Test
    fun `GIVEN a measured viewport WHEN pinching to zoom in THEN lets the photo pan further`() = runTest {
        // Given
        val viewModel = viewModel(decode = { Result.success(landscapeBitmap) })
        advanceUntilIdle()
        viewModel.onEvent(
            CropPhotoUiEvent.OnViewportMeasured(
                areaWidth = 300f,
                areaHeight = 300f,
                circleDiameter = 100f,
            ),
        )

        // When
        viewModel.onEvent(
            CropPhotoUiEvent.OnTransform(
                panX = -500f,
                panY = 500f,
                zoomChange = 2f,
            ),
        )

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(2f, uiState.zoom)
        assertEquals(-150f, uiState.offsetX)
        assertEquals(50f, uiState.offsetY)
    }

    @Test
    fun `GIVEN the crop screen WHEN flipping vertically THEN turns the photo upside down`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnFlipVerticalClick)

        // Then
        assertTrue(viewModel.uiState.value.orientation.isFlippedVertically)
    }

    private suspend fun TestScope.actionsAfter(event: CropPhotoUiEvent): List<CropPhotoUiAction> {
        val viewModel = viewModel()
        val actions = mutableListOf<CropPhotoUiAction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiAction.collect { actions.add(it) }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { navigator.commands.collect(commands::add) }
        viewModel.onEvent(event)
        advanceUntilIdle()
        job.cancel()
        return actions.toList()
    }

    private fun viewModel(decode: DecodeImageBitmap = DecodeImageBitmap { awaitCancellation() }): CropPhotoViewModel =
        CropPhotoViewModel(
            route = CropPhotoNavRoute(PlatformFile("unused.jpg")),
            decodeImageBitmap = decode,
            cropImage = { _, _ -> byteArrayOf(9) },
            setProfilePhoto = { savedPhotos.add(it) },
            navigator = navigator,
            trackEvent = { _, _ -> },
        )
}
