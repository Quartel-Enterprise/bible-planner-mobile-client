package com.quare.bibleplanner.core.navigation

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination

/*
 * Why: on iOS the shared main screen still draws the tab bar, so a tab migrated to SwiftUI swaps only its body
 * inside the shared tab scaffold (docs/architecture/native-ui-migration.md). An app binds this to draw those
 * bodies, and null keeps the shared tab. Android draws its whole main screen natively instead.
 */
fun interface NativeTabContentProvider {
    fun contentFor(tab: MainNavRouteDestination): (@Composable () -> Unit)?
}
