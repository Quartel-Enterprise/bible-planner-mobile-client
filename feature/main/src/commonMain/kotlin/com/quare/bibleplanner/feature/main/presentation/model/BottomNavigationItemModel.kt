package com.quare.bibleplanner.feature.main.presentation.model

data class BottomNavigationItemModel<T : Any>(
    val presentationModel: BottomNavigationItemPresentationModel,
    val route: T,
)
