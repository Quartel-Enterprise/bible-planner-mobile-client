package com.quare.bibleplanner.ui.component.dialog

data class AppAlertDialogAction(
    val text: String,
    val style: AppAlertDialogActionStyle,
    val isEnabled: Boolean,
    val onClick: () -> Unit,
)
