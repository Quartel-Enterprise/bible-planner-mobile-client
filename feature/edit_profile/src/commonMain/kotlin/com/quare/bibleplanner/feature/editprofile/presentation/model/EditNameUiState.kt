package com.quare.bibleplanner.feature.editprofile.presentation.model

import software.quare.loadable.Loadable

internal data class EditNameUiState(
    val currentName: Loadable<String?>,
)
