package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import bibleplanner.feature.main.generated.resources.Res
import bibleplanner.feature.main.generated.resources.books
import bibleplanner.feature.main.generated.resources.plans
import bibleplanner.feature.main.generated.resources.profile
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import org.jetbrains.compose.resources.StringResource

internal fun NavKey.toTabTitle(): StringResource = when (this as MainNavRouteDestination) {
    MainNavRouteDestination.Plans -> Res.string.plans
    MainNavRouteDestination.Books -> Res.string.books
    MainNavRouteDestination.Profile -> Res.string.profile
}

internal fun NavKey.toTabIcon(): ImageVector = when (this as MainNavRouteDestination) {
    MainNavRouteDestination.Plans -> Icons.Default.DateRange
    MainNavRouteDestination.Books -> Icons.AutoMirrored.Filled.MenuBook
    MainNavRouteDestination.Profile -> Icons.Default.Person
}
