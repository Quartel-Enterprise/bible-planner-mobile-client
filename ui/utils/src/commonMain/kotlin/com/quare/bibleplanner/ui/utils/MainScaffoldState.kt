package com.quare.bibleplanner.ui.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf

@Stable
class MainScaffoldState(
    val snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val topBar: MutableState<@Composable () -> Unit> = mutableStateOf({})
    val fab: MutableState<@Composable () -> Unit> = mutableStateOf({})
    
    fun setTopBar(content: @Composable () -> Unit) {
        topBar.value = content
    }

    fun setFab(content: @Composable () -> Unit) {
        fab.value = content
    }

    fun clearTopBar() {
        topBar.value = {}
    }

    fun clearFab() {
        fab.value = {}
    }
}
