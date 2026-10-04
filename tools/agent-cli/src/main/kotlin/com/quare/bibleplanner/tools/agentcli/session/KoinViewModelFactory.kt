package com.quare.bibleplanner.tools.agentcli.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation3.runtime.NavKey
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

class KoinViewModelFactory(
    private val koin: Koin,
    private val parameter: NavKey?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: KClass<T>,
        extras: CreationExtras,
    ): T = koin.get(
        clazz = modelClass,
        qualifier = null,
        parameters = parameter?.let { navKey -> { parametersOf(navKey) } },
    )
}
