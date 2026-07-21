package com.quare.bibleplanner.feature.editprofile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_removed
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_synced
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_too_large
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_unreadable
import com.quare.bibleplanner.core.image.MAX_AVATAR_SOURCE_BYTES
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import com.quare.bibleplanner.core.profile.domain.usecase.ObserveUserProfile
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.editprofile.domain.usecase.RemoveProfilePhoto
import com.quare.bibleplanner.feature.editprofile.domain.usecase.UseProviderPhoto
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.ProfilePhotoUiState
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

internal class ProfilePhotoViewModel(
    observeUserProfile: ObserveUserProfile,
    private val removeProfilePhoto: RemoveProfilePhoto,
    private val useProviderPhoto: UseProviderPhoto,
    platform: Platform,
    trackEvent: TrackEvent,
) : TrackedViewModel<ProfilePhotoUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<ProfilePhotoUiAction>()
    val uiAction: SharedFlow<ProfilePhotoUiAction> = _uiAction

    private val isCameraAvailable: Boolean = platform !is Platform.Desktop

    val uiState: StateFlow<ProfilePhotoUiState> = observeUserProfile()
        .map { profile ->
            ProfilePhotoUiState(
                profile = Loadable.Loaded(profile),
                isCameraAvailable = isCameraAvailable,
            )
        }.onStart { emit(initialState()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialState(),
        )

    override fun handleEvent(event: ProfilePhotoUiEvent) {
        when (event) {
            ProfilePhotoUiEvent.OnUseProviderPhotoClick -> fallBackToProviderPhoto()
            ProfilePhotoUiEvent.OnRemovePhotoClick -> removePhoto()
            ProfilePhotoUiEvent.OnPickFromGalleryClick -> emit(ProfilePhotoUiAction.LaunchGalleryPicker)
            ProfilePhotoUiEvent.OnTakePhotoClick -> emit(ProfilePhotoUiAction.LaunchCameraPicker)
            is ProfilePhotoUiEvent.OnImagePicked -> onImagePicked(event.file)
        }
    }

    private fun onImagePicked(file: PlatformFile?) {
        if (file == null) return
        viewModelScope.launch {
            val size = suspendRunCatching { file.size() }.getOrElse {
                showSnackbar(Res.string.edit_profile_photo_unreadable)
                return@launch
            }
            if (size > MAX_AVATAR_SOURCE_BYTES) {
                showSnackbar(Res.string.edit_profile_photo_too_large)
                return@launch
            }
            _uiAction.emit(ProfilePhotoUiAction.OpenCrop(CropPhotoNavRoute(file)))
        }
    }

    private fun fallBackToProviderPhoto() {
        viewModelScope.launch {
            useProviderPhoto()
            finishWith(Res.string.edit_profile_photo_synced)
        }
    }

    private fun removePhoto() {
        viewModelScope.launch {
            removeProfilePhoto()
            finishWith(Res.string.edit_profile_photo_removed)
        }
    }

    private suspend fun finishWith(message: StringResource) {
        _uiAction.emit(ProfilePhotoUiAction.PhotoChanged)
        _uiAction.emit(ProfilePhotoUiAction.ShowSnackbar(message))
    }

    private suspend fun showSnackbar(message: StringResource) {
        _uiAction.emit(ProfilePhotoUiAction.ShowSnackbar(message))
    }

    private fun emit(action: ProfilePhotoUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private fun initialState(): ProfilePhotoUiState = ProfilePhotoUiState(
        profile = Loadable.Loading,
        isCameraAvailable = isCameraAvailable,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
