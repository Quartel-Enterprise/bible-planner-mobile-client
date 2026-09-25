package com.quare.bibleplanner.ui.component

import com.quare.bibleplanner.ui.icons.AppIcon

data class AppDropdownMenuItem(
    val title: String,
    val icon: AppIcon?,
    val isSelected: Boolean,
    val isDestructive: Boolean,
    val onClick: () -> Unit,
)
