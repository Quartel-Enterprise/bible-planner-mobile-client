package com.quare.bibleplanner.feature.editprofile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_unreadable
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_photo_updated
import com.quare.bibleplanner.core.image.AvatarImageCropper
import com.quare.bibleplanner.core.image.CropParams
import com.quare.bibleplanner.core.image.maxPanOffset
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.editprofile.domain.usecase.SetProfilePhoto
import com.quare.bibleplanner.feature.editprofile.presentation.DecodeImageBitmap
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiState
import com.quare.bibleplanner.feature.editprofile.presentation.model.ImageResult
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource

internal class CropPhotoViewModel(
    private val route: CropPhotoNavRoute,
    private val decodeImageBitmap: DecodeImageBitmap,
    private val cropImage: AvatarImageCropper,
    private val setProfilePhoto: SetProfilePhoto,
    trackEvent: TrackEvent,
    private val encodeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : TrackedViewModel<CropPhotoUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<CropPhotoUiAction>()
    val uiAction: SharedFlow<CropPhotoUiAction> = _uiAction

    private val _uiState = MutableStateFlow(
        CropPhotoUiState(
            image = ImageResult.Loading,
            zoom = MIN_ZOOM,
            zoomRange = MIN_ZOOM..MAX_ZOOM,
            offsetX = 0f,
            offsetY = 0f,
        ),
    )
    val uiState: StateFlow<CropPhotoUiState> = _uiState

    private var imageWidth = 0
    private var imageHeight = 0
    private var circleDiameter = 1f

    init {
        viewModelScope.launch {
            decodeImageBitmap(route.file)
                .onSuccess { bitmap ->
                    imageWidth = bitmap.width
                    imageHeight = bitmap.height
                    _uiState.update { it.copy(image = ImageResult.Loaded(bitmap)).clamped() }
                }.onFailure {
                    _uiState.update { it.copy(image = ImageResult.Failed) }
                    finishWith(Res.string.edit_profile_photo_unreadable)
                }
        }
    }

    override fun handleEvent(event: CropPhotoUiEvent) {
        when (event) {
            is CropPhotoUiEvent.OnViewportMeasured -> onViewportMeasured(event)
            is CropPhotoUiEvent.OnTransform -> onTransform(event)
            is CropPhotoUiEvent.OnZoomChanged -> setZoom(event.zoom)
            CropPhotoUiEvent.OnCancelClick -> emit(CropPhotoUiAction.NavigateBack)
            CropPhotoUiEvent.OnConfirmClick -> confirmCrop()
        }
    }

    private fun onViewportMeasured(event: CropPhotoUiEvent.OnViewportMeasured) {
        circleDiameter = event.circleDiameter
        _uiState.update { it.clamped() }
    }

    private fun onTransform(event: CropPhotoUiEvent.OnTransform) {
        _uiState.update { state ->
            state
                .copy(
                    zoom = (state.zoom * event.zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM),
                    offsetX = state.offsetX + event.panX,
                    offsetY = state.offsetY + event.panY,
                ).clamped()
        }
    }

    private fun setZoom(zoom: Float) {
        _uiState.update { it.copy(zoom = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)).clamped() }
    }

    private fun confirmCrop() {
        if (imageWidth <= 0 || imageHeight <= 0) return
        viewModelScope.launch {
            val encoded = suspendRunCatching {
                withContext(encodeDispatcher) {
                    cropImage(
                        source = route.file.readBytes(),
                        params = CropParams(
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            circleDiameter = circleDiameter,
                            zoom = _uiState.value.zoom,
                            offsetX = _uiState.value.offsetX,
                            offsetY = _uiState.value.offsetY,
                        ),
                    )
                }
            }.getOrNull()
            if (encoded == null) {
                finishWith(Res.string.edit_profile_photo_unreadable)
                return@launch
            }
            setProfilePhoto(encoded)
            finishWith(Res.string.edit_profile_photo_updated)
        }
    }

    private suspend fun finishWith(message: StringResource) {
        _uiAction.emit(CropPhotoUiAction.NavigateBack)
        _uiAction.emit(CropPhotoUiAction.ShowSnackbar(message))
    }

    private fun emit(action: CropPhotoUiAction) {
        viewModelScope.launch { _uiAction.emit(action) }
    }

    private fun CropPhotoUiState.clamped(): CropPhotoUiState {
        if (imageWidth <= 0 || imageHeight <= 0) return this
        val (maxX, maxY) = maxPanOffset(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            circleDiameter = circleDiameter,
            zoom = zoom,
        )
        return copy(
            offsetX = offsetX.coerceIn(-maxX, maxX),
            offsetY = offsetY.coerceIn(-maxY, maxY),
        )
    }

    private companion object {
        const val MIN_ZOOM = 1f
        const val MAX_ZOOM = 3f
    }
}
