package com.quare.bibleplanner.feature.editprofile.presentation

import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiEvent
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
    fun `keeps the zoom within its bounds`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnZoomChanged(zoom = 99f))

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(uiState.zoomRange.endInclusive, uiState.zoom)
    }

    @Test
    fun `mirrors the photo when the user flips it`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnFlipHorizontalClick)

        // Then
        assertTrue(viewModel.uiState.value.orientation.isFlippedHorizontally)
    }

    @Test
    fun `turns the photo a quarter clockwise on each rotate tap`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnRotateClick)
        viewModel.onEvent(CropPhotoUiEvent.OnRotateClick)

        // Then
        assertEquals(180, viewModel.uiState.value.orientation.rotationDegrees)
    }

    @Test
    fun `ignores confirm until the image has loaded`() = runTest {
        // Given — the decoder never completes, so no image is ready
        val viewModel = viewModel()

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnConfirmClick)
        advanceUntilIdle()

        // Then — nothing is cropped or saved before the image loads
        assertTrue(savedPhotos.isEmpty())
    }

    @Test
    fun `navigates back when the user cancels`() = runTest {
        // When
        val actions = actionsAfter(CropPhotoUiEvent.OnCancelClick)

        // Then
        assertTrue(actions.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
    }

    @Test
    fun `navigates back and warns when the image cannot be decoded`() = runTest {
        // Given — a decoder that returns null (unreadable image)
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
