package com.quare.bibleplanner.tools.agentcli.session

import androidx.lifecycle.ViewModelStore
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class HeadlessEntry(
    val route: NavKey?,
    val viewModels: List<HeadlessViewModel>,
    private val viewModelStore: ViewModelStore,
    private val scope: CoroutineScope,
) {
    fun close() {
        scope.cancel()
        viewModelStore.clear()
    }
}
