package com.quare.bibleplanner.tools.agentcli.catalog

import androidx.lifecycle.ViewModel
import org.koin.core.Koin
import org.koin.core.annotation.KoinInternalApi
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class ViewModelCatalog(
    val viewModels: List<KClass<out ViewModel>>,
) {
    fun find(simpleName: String): KClass<out ViewModel> {
        val matches = viewModels.filter { viewModel -> viewModel.simpleName == simpleName }
        return when (matches.size) {
            1 -> matches.single()
            0 -> throw IllegalArgumentException("no ViewModel named $simpleName in the Koin graph")
            else -> throw IllegalArgumentException("$simpleName is ambiguous: ${matches.map(KClass<*>::qualifiedName)}")
        }
    }

    fun findOrNull(simpleName: String): KClass<out ViewModel>? = viewModels.firstOrNull { it.simpleName == simpleName }

    companion object {
        @OptIn(KoinInternalApi::class)
        @Suppress("UNCHECKED_CAST")
        fun from(koin: Koin): ViewModelCatalog = ViewModelCatalog(
            koin.instanceRegistry.instances.values
                .map { factory -> factory.beanDefinition.primaryType }
                .filter { type -> type.isSubclassOf(ViewModel::class) }
                .map { type -> type as KClass<out ViewModel> }
                .distinct()
                .sortedBy { type -> type.simpleName },
        )
    }
}
