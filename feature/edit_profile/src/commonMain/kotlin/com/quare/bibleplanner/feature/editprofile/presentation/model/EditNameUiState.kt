package com.quare.bibleplanner.feature.editprofile.presentation.model

import com.quare.bibleplanner.core.model.loadable.Loadable

internal data class EditNameUiState(
    val currentName: Loadable<String?>,
)
