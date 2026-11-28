package com.quare.bibleplanner.feature.editprofile.presentation.model

import software.quare.loadable.Loadable

data class EditNameUiState(
    val currentName: Loadable<String?>,
)
