package com.quare.bibleplanner.nativeui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.navigation.NativeTabContentProvider
import com.quare.bibleplanner.ui.utils.LocalMainPadding

internal class IosNativeTabContentProvider(
    private val screenFactory: NativeScreenFactory,
) : NativeTabContentProvider {
    override fun contentFor(tab: MainNavRouteDestination): (@Composable () -> Unit)? = when (tab) {
        MainNavRouteDestination.Plans -> {
            {
                NativeTabContent(
                    key = READING_PLAN_KEY,
                    createScreen = screenFactory::createReadingPlanScreen,
                )
            }
        }

        MainNavRouteDestination.Books, MainNavRouteDestination.Profile -> null
    }

    private companion object {
        const val READING_PLAN_KEY = "native_reading_plan_screen"
    }
}

@Composable
private fun NativeTabContent(
    key: String,
    createScreen: () -> NativeScreen,
) {
    /*
     * Why: UIKit lays the screen out under the status bar on its own, but it doesn't know about the shared tab bar
     * Compose draws at the bottom, so only that edge is padded.
     */
    NativeScreenContent(
        key = key,
        createScreen = createScreen,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = LocalMainPadding.current.calculateBottomPadding()),
    )
}
