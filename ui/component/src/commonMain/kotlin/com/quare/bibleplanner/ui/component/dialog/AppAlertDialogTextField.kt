package com.quare.bibleplanner.ui.component.dialog

data class AppAlertDialogTextField(
    val value: String,
    val label: String,
    val onValueChange: (String) -> Unit,
)
