package com.quare.bibleplanner.feature.editprofile.presentation

import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_unreadable
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_updated
import com.quare.bibleplanner.core.image.AvatarImageCropper
import com.quare.bibleplanner.core.image.CropParams
import com.quare.bibleplanner.core.model.NavigationCommand
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.feature.editprofile.fake.FakeImageBitmap
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.CropPhotoViewModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class CropPhotoViewModelConfirmTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val fileReadTimeout = 5.seconds
    private val photoBytes = byteArrayOf(1, 2, 3)
    private lateinit var photoFile: File
    private lateinit var viewModel: CropPhotoViewModel
    private lateinit var savedPhotos: MutableList<ByteArray>
    private lateinit var cropCalls: MutableList<Pair<ByteArray, CropParams>>
    private lateinit var commands: MutableList<NavigationCommand>
    private lateinit var actions: Channel<CropPhotoUiAction>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        photoFile = File.createTempFile("crop", ".jpg").apply {
            writeBytes(photoBytes)
            deleteOnExit()
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        photoFile.delete()
    }

    @Test
    fun `GIVEN a loaded photo WHEN confirming the crop THEN crops the file bytes with the photo size`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cropper = { _, _ -> byteArrayOf(9) })

            // When
            viewModel.onEvent(CropPhotoUiEvent.OnConfirmClick)
            val action = awaitAction()

            // Then
            assertEquals(CropPhotoUiAction.ShowSnackbar(Res.string.edit_profile_photo_updated), action)
            val (source, params) = cropCalls.single()
            assertContentEquals(photoBytes, source)
            assertEquals(200, params.imageWidth)
            assertEquals(100, params.imageHeight)
            assertEquals(1f, params.zoom)
        }

    @Test
    fun `GIVEN a loaded photo WHEN confirming the crop THEN saves the cropped photo and confirms it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(cropper = { _, _ -> byteArrayOf(9) })

            // When
            viewModel.onEvent(CropPhotoUiEvent.OnConfirmClick)
            val action = awaitAction()

            // Then
            assertContentEquals(byteArrayOf(9), savedPhotos.single())
            assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
            assertEquals(CropPhotoUiAction.ShowSnackbar(Res.string.edit_profile_photo_updated), action)
        }

    @Test
    fun `GIVEN a cropper failure WHEN confirming the crop THEN warns without saving`() = runTest(testDispatcher) {
        // Given
        prepareScenario(cropper = { _, _ -> error("boom") })

        // When
        viewModel.onEvent(CropPhotoUiEvent.OnConfirmClick)
        val action = awaitAction()

        // Then
        assertTrue(savedPhotos.isEmpty())
        assertEquals(listOf<NavigationCommand>(NavigationCommand.NavigateBack), commands)
        assertEquals(CropPhotoUiAction.ShowSnackbar(Res.string.edit_profile_photo_unreadable), action)
    }

    private suspend fun awaitAction(): CropPhotoUiAction = withContext(Dispatchers.Default) {
        withTimeout(fileReadTimeout) { actions.receive() }
    }

    private fun TestScope.prepareScenario(cropper: AvatarImageCropper) {
        val navigator = Navigator()
        savedPhotos = mutableListOf()
        cropCalls = mutableListOf()
        commands = mutableListOf()
        actions = Channel(Channel.UNLIMITED)
        backgroundScope.launch { navigator.commands.collect(commands::add) }
        viewModel = CropPhotoViewModel(
            route = CropPhotoNavRoute(PlatformFile(photoFile)),
            cropImage = { source, params ->
                cropCalls += source to params
                cropper(
                    source = source,
                    params = params,
                )
            },
            setProfilePhoto = { bytes -> savedPhotos += bytes },
            navigator = navigator,
            encodeDispatcher = testDispatcher,
            decodeImageBitmap = {
                Result.success(
                    FakeImageBitmap(
                        width = 200,
                        height = 100,
                    ),
                )
            },
            trackEvent = { _, _ -> },
        )
        backgroundScope.launch { viewModel.uiAction.collect(actions::send) }
    }
}
