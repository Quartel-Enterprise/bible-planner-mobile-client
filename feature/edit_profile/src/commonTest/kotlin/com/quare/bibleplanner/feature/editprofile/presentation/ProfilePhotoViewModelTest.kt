package com.quare.bibleplanner.feature.editprofile.presentation

import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.viewmodel.ProfilePhotoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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

class ProfilePhotoViewModelTest {
    private var removePhotoCalls = 0
    private var useProviderPhotoCalls = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        removePhotoCalls = 0
        useProviderPhotoCalls = 0
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN the photo sheet WHEN removing the photo THEN removes it and closes the sheet`() = runTest {
        // When
        val actions = actionsAfter(ProfilePhotoUiEvent.OnRemovePhotoClick)

        // Then
        assertEquals(ProfilePhotoUiAction.PhotoChanged, actions.first())
        assertEquals(1, removePhotoCalls)
    }

    @Test
    fun `GIVEN the photo sheet WHEN using the provider photo THEN falls back to it and closes the sheet`() = runTest {
        // When
        val actions = actionsAfter(ProfilePhotoUiEvent.OnUseProviderPhotoClick)

        // Then
        assertEquals(ProfilePhotoUiAction.PhotoChanged, actions.first())
        assertEquals(1, useProviderPhotoCalls)
    }

    @Test
    fun `GIVEN the photo sheet WHEN picking from the gallery THEN asks the ui to open the gallery picker`() = runTest {
        // When
        val actions = actionsAfter(ProfilePhotoUiEvent.OnPickFromGalleryClick)

        // Then
        assertEquals(listOf(ProfilePhotoUiAction.LaunchGalleryPicker), actions)
    }

    @Test
    fun `GIVEN the photo sheet WHEN taking a photo THEN asks the ui to open the camera picker`() = runTest {
        // When
        val actions = actionsAfter(ProfilePhotoUiEvent.OnTakePhotoClick)

        // Then
        assertEquals(listOf(ProfilePhotoUiAction.LaunchCameraPicker), actions)
    }

    @Test
    fun `GIVEN the photo sheet WHEN the picker is cancelled THEN ignores it`() = runTest {
        // When
        val actions = actionsAfter(ProfilePhotoUiEvent.OnImagePicked(null))

        // Then
        assertTrue(actions.isEmpty())
    }

    @Test
    fun `GIVEN a desktop platform WHEN opening the photo sheet THEN hides the camera option`() = runTest {
        // When
        val viewModel = viewModel(platform = Platform.Desktop.MacOs)

        // Then
        assertTrue(!viewModel.uiState.value.isCameraAvailable)
    }

    private suspend fun TestScope.actionsAfter(event: ProfilePhotoUiEvent): List<ProfilePhotoUiAction> {
        val viewModel = viewModel()
        val actions = mutableListOf<ProfilePhotoUiAction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiAction.collect(actions::add)
        }
        viewModel.onEvent(event)
        advanceUntilIdle()
        job.cancel()
        return actions
    }

    private fun viewModel(platform: Platform = Platform.Android): ProfilePhotoViewModel = ProfilePhotoViewModel(
        observeUserProfile = ObserveUserProfile { flowOf(profile()) },
        removeProfilePhoto = { removePhotoCalls++ },
        useProviderPhoto = { useProviderPhotoCalls++ },
        platform = platform,
        trackEvent = { _, _ -> },
    )

    private fun profile(): UserProfile = UserProfile(
        userId = "user-id",
        displayName = "Current Name",
        email = "user@example.com",
        avatar = AvatarSource.None,
        hasVisiblePhoto = false,
        hasProviderPhoto = true,
        isUsingProviderPhoto = true,
        provider = "google",
    )
}
