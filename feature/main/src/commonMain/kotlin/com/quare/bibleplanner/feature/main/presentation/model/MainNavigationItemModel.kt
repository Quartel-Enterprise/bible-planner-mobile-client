package com.quare.bibleplanner.feature.main.presentation.model

import androidx.navigation3.runtime.NavKey

data class MainNavigationItemModel<T : NavKey>(
    val presentationModel: MainNavigationItemPresentationModel,
    val route: T,
)
