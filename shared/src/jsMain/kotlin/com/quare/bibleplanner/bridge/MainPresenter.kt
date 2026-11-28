@file:JsExport

package com.quare.bibleplanner.bridge

import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.presentation.bridge.ViewModelHolder
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import com.quare.bibleplanner.feature.main.presentation.viewmodel.MainScreenViewModel
import org.koin.mp.KoinPlatform

/*
 * Why: TypeScript can't use a sealed type, a generic NavKey or a Kotlin List, so the tab crosses as plain values:
 * key is "plans", "books" or "profile", and profileName is the signed in user's display name on the profile tab.
 */
class JsMainTab internal constructor(
    val key: String,
    val profileName: String?,
)

/*
 * Why: the JS analogue of NativeViewModels.createMain(). Members of a Kotlin class that isn't exported get mangled
 * names in JS, so React reaches the ViewModel through this presenter, which maps its state to exported types and
 * clears its ViewModelHolder on dispose().
 */
class MainPresenter {
    private val holder = ViewModelHolder(MainScreenViewModel::class) { KoinPlatform.getKoin().get() }

    fun observeTabs(onChange: (Array<JsMainTab>) -> Unit): JsWatch {
        val watch = holder.viewModel.stateWatcher.observe { items ->
            onChange(items.map(::toJsMainTab).toTypedArray())
        }
        return JsWatch(watch::cancel)
    }

    fun dispose() {
        holder.clear()
    }
}

private fun toJsMainTab(item: MainNavigationItemModel<MainNavRouteDestination>): JsMainTab = JsMainTab(
    key = when (item.route) {
        MainNavRouteDestination.Plans -> "plans"
        MainNavRouteDestination.Books -> "books"
        MainNavRouteDestination.Profile -> "profile"
    },
    profileName = (item.presentationModel.icon as? MainNavigationIcon.Profile)?.displayName,
)
