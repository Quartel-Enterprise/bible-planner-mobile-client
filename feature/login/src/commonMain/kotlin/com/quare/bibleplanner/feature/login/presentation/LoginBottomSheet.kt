package com.quare.bibleplanner.feature.login.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.quare.bibleplanner.feature.login.presentation.model.LoginUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(onEvent: (LoginUiEvent) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onEvent(LoginUiEvent.Dismiss) }) {
        Text("Login bottom sheet")
    }
}
