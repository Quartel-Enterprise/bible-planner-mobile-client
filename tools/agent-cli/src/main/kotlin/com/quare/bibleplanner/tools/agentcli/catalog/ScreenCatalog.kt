package com.quare.bibleplanner.tools.agentcli.catalog

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/*
 * Why: which ViewModels a route shows is decided inside each feature's entry composable, which can't
 * be read headlessly. Most routes follow a convention (the ViewModel takes the route, or is named
 * after it), so only the exceptions are listed, in AppScreens; AppScreensTest fails when a new route
 * matches neither.
 */
class ScreenCatalog(
    private val viewModelCatalog: ViewModelCatalog,
    private val viewModelsByRoute: Map<String, List<String>>,
    private val routeParameters: Map<String, (NavKey) -> NavKey>,
    appViewModelNames: List<String>,
) {
    val appViewModels: List<KClass<out ViewModel>> = appViewModelNames.map(viewModelCatalog::find)

    fun parameterFor(route: NavKey): NavKey = routeParameters[route::class.simpleName]?.invoke(route) ?: route

    fun viewModelsFor(route: KClass<out NavKey>): List<KClass<out ViewModel>> {
        viewModelsByRoute[route.simpleName]?.let { names -> return names.map(viewModelCatalog::find) }
        val takingRoute = viewModelCatalog.viewModels.filter { viewModel ->
            viewModel.primaryConstructor
                ?.parameters
                .orEmpty()
                .any { parameter -> parameter.type.classifier == route }
        }
        if (takingRoute.isNotEmpty()) return takingRoute
        val baseName = route.simpleName
            .orEmpty()
            .removeSuffix("NavRoute")
            .removeSuffix("Route")
        return listOfNotNull(viewModelCatalog.findOrNull("${baseName}ViewModel"))
    }
}
