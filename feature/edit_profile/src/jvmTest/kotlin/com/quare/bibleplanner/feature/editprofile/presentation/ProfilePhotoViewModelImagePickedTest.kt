package com.quare.bibleplanner.feature.editprofile.presentation

import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_too_large
import com.quare.bibleplanner.core.image.MAX_AVATAR_SOURCE_BYTES
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.File
import java.io.RandomAccessFile
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class ProfilePhotoViewModelImagePickedTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var pickedFile: File
    private lateinit var viewModel: ProfilePhotoViewModel
    private lateinit var actions: MutableList<ProfilePhotoUiAction>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        pickedFile = File.createTempFile("picked", ".jpg").apply { deleteOnExit() }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        pickedFile.delete()
    }

    @Test
    fun `GIVEN a photo within the size limit WHEN it is picked THEN opens the crop screen for it`() =
        runTest(testDispatcher) {
            // Given
            prepareScenario(fileSizeBytes = 1_024L)

            // When
            viewModel.onEvent(ProfilePhotoUiEvent.OnImagePicked(PlatformFile(pickedFile)))
            advanceUntilIdle()

            // Then
            assertEquals(
                listOf<ProfilePhotoUiAction>(
                    ProfilePhotoUiAction.OpenCrop(CropPhotoNavRoute(PlatformFile(pickedFile))),
                ),
                actions,
            )
        }

    @Test
    fun `GIVEN a photo over the size limit WHEN it is picked THEN warns it is too large`() = runTest(testDispatcher) {
        // Given
        prepareScenario(fileSizeBytes = MAX_AVATAR_SOURCE_BYTES + 1L)

        // When
        viewModel.onEvent(ProfilePhotoUiEvent.OnImagePicked(PlatformFile(pickedFile)))
        advanceUntilIdle()

        // Then
        assertEquals(
            listOf<ProfilePhotoUiAction>(ProfilePhotoUiAction.ShowSnackbar(Res.string.edit_profile_photo_too_large)),
            actions,
        )
    }

    private fun TestScope.prepareScenario(fileSizeBytes: Long) {
        RandomAccessFile(pickedFile, "rw").use { file -> file.setLength(fileSizeBytes) }
        actions = mutableListOf()
        viewModel = ProfilePhotoViewModel(
            removeProfilePhoto = {},
            useProviderPhoto = {},
            observeUserProfile = ObserveUserProfile(::emptyFlow),
            platform = Platform.Android,
            trackEvent = { _, _ -> },
        )
        backgroundScope.launch { viewModel.uiAction.collect(actions::add) }
    }
}
