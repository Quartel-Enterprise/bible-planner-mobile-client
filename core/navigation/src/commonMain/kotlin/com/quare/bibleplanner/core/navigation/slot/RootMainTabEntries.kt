package com.quare.bibleplanner.core.navigation.slot

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.navigation.NativeTabContentProvider
import com.quare.bibleplanner.feature.books.presentation.booksScreen
import com.quare.bibleplanner.feature.main.presentation.MainTabEntries
import com.quare.bibleplanner.feature.profile.presentation.profile
import com.quare.bibleplanner.feature.readingplan.presentation.readingPlan
import com.quare.bibleplanner.ui.utils.MainTabScaffold

internal class RootMainTabEntries(
    private val sharedTransitionScope: SharedTransitionScope,
    private val nativeTabContent: NativeTabContentProvider? = null,
) : MainTabEntries {
    override fun register(
        scope: EntryProviderScope<NavKey>,
        navigationBar: @Composable (Modifier) -> Unit,
        navigationRail: @Composable () -> Unit,
        animatedContentScope: AnimatedContentScope,
    ) {
        val nativePlans = nativeTabContent?.contentFor(MainNavRouteDestination.Plans)
        if (nativePlans == null) {
            scope.readingPlan(
                navigationBar = navigationBar,
                navigationRail = navigationRail,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        } else {
            scope.entry<MainNavRouteDestination.Plans> {
                MainTabScaffold(
                    navigationBar = navigationBar,
                    navigationRail = navigationRail,
                    content = nativePlans,
                )
            }
        }
        scope.booksScreen(
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedContentScope,
        )
        scope.profile(
            navigationBar = navigationBar,
            navigationRail = navigationRail,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
        )
    }
}
