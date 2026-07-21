package com.quare.bibleplanner.feature.editprofile.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.quare.bibleplanner.core.model.route.EditNameNavRoute
import com.quare.bibleplanner.core.model.route.EditPhotoSourceNavRoute
import com.quare.bibleplanner.core.model.route.NavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.TrackEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileUiAction
import com.quare.bibleplanner.feature.editprofile.presentation.model.EditProfileUiEvent
import com.quare.bibleplanner.ui.utils.presentation.TrackedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal class EditProfileViewModel(
    trackEvent: TrackEvent,
) : TrackedViewModel<EditProfileUiEvent>(trackEvent) {
    private val _uiAction = MutableSharedFlow<EditProfileUiAction>()
    val uiAction: SharedFlow<EditProfileUiAction> = _uiAction

    override fun handleEvent(event: EditProfileUiEvent) {
        when (event) {
            EditProfileUiEvent.OnChangeNameClick -> replaceWith(EditNameNavRoute)
            EditProfileUiEvent.OnChangeImageClick -> replaceWith(EditPhotoSourceNavRoute)
        }
    }

    private fun replaceWith(route: NavRoute) {
        viewModelScope.launch { _uiAction.emit(EditProfileUiAction(route)) }
    }
}
