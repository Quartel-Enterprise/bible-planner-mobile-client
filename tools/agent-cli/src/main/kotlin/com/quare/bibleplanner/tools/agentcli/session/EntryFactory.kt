package com.quare.bibleplanner.tools.agentcli.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.Koin
import kotlin.reflect.KClass

class EntryFactory(
    private val koin: Koin,
    private val log: SessionLog,
    private val encoder: StateEncoder,
) {
    fun create(
        route: NavKey?,
        parameter: NavKey?,
        viewModelClasses: List<KClass<out ViewModel>>,
    ): HeadlessEntry {
        val viewModelStore = ViewModelStore()
        val provider = ViewModelProvider.create(
            store = viewModelStore,
            factory = KoinViewModelFactory(
                koin = koin,
                parameter = parameter,
            ),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val viewModels = runCatching {
            viewModelClasses.map { viewModelClass ->
                HeadlessViewModel(
                    viewModel = provider[viewModelClass],
                    scope = scope,
                    log = log,
                    encoder = encoder,
                )
            }
        }.getOrElse { error ->
            scope.cancel()
            viewModelStore.clear()
            throw error
        }
        return HeadlessEntry(
            route = route,
            viewModels = viewModels,
            viewModelStore = viewModelStore,
            scope = scope,
        )
    }
}
