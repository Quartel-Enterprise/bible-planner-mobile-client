package com.quare.bibleplanner.feature.more.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.privacy_policy
import bibleplanner.feature.more.generated.resources.terms_of_service
import bibleplanner.feature.more.generated.resources.theme_option
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent

internal object MoreMenuOptionsFactory {
    val options = listOf(
        MoreMenuItemPresentationModel(
            name = Res.string.theme_option,
            icon = Icons.Default.Palette,
            event = MoreUiEvent.ON_THEME_CLICK,
        ),
        MoreMenuItemPresentationModel(
            name = Res.string.terms_of_service,
            icon = Icons.Default.Info,
            event = MoreUiEvent.ON_TERMS_CLICK,
        ),
        MoreMenuItemPresentationModel(
            name = Res.string.privacy_policy,
            icon = Icons.Default.Shield,
            event = MoreUiEvent.ON_PRIVACY_CLICK,
        ),
    )
}
