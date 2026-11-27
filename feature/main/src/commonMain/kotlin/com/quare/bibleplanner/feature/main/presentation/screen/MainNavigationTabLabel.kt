package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.feature.main.generated.resources.Res
import bibleplanner.feature.main.generated.resources.books
import bibleplanner.feature.main.generated.resources.plans
import bibleplanner.feature.main.generated.resources.profile
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import org.jetbrains.compose.resources.StringResource

internal fun MainNavRouteDestination.toTabTitle(): StringResource = when (this) {
    MainNavRouteDestination.Plans -> Res.string.plans
    MainNavRouteDestination.Books -> Res.string.books
    MainNavRouteDestination.Profile -> Res.string.profile
}

internal fun MainNavRouteDestination.toTabIcon(): ImageVector = when (this) {
    MainNavRouteDestination.Plans -> Icons.Default.DateRange
    MainNavRouteDestination.Books -> Icons.AutoMirrored.Filled.MenuBook
    MainNavRouteDestination.Profile -> Icons.Default.Person
}
