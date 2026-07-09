package com.quare.bibleplanner.feature.main.presentation.model

import androidx.navigation3.runtime.NavKey

data class BottomNavigationItemModel<T : NavKey>(
    val presentationModel: BottomNavigationItemPresentationModel,
    val route: T,
)
