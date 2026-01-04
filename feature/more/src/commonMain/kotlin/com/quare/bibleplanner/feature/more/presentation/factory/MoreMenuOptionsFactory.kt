package com.quare.bibleplanner.feature.more.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_premium
import bibleplanner.feature.more.generated.resources.delete_progress_option
import bibleplanner.feature.more.generated.resources.ic_instagram
import bibleplanner.feature.more.generated.resources.instagram
import bibleplanner.feature.more.generated.resources.privacy_policy
import bibleplanner.feature.more.generated.resources.start_date
import bibleplanner.feature.more.generated.resources.terms_of_service
import bibleplanner.feature.more.generated.resources.theme_option
import com.quare.bibleplanner.feature.more.presentation.model.MoreIcon
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreUiEvent

internal object MoreMenuOptionsFactory {
    val baseOptions = listOf(
        MoreMenuItemPresentationModel(
            name = Res.string.theme_option,
            icon = MoreIcon.ImageVectorIcon(Icons.Default.Palette),
            event = MoreUiEvent.ON_THEME_CLICK,
        ),
        MoreMenuItemPresentationModel(
            name = Res.string.start_date,
            icon = MoreIcon.ImageVectorIcon(Icons.Default.EditCalendar),
            event = MoreUiEvent.ON_EDIT_PLAN_START_DAY_CLICK,
        ),
        MoreMenuItemPresentationModel(
            name = Res.string.delete_progress_option,
            icon = MoreIcon.ImageVectorIcon(Icons.Default.Delete),
            event = MoreUiEvent.ON_DELETE_PROGRESS_CLICK,
        ),
        MoreMenuItemPresentationModel(
            name = Res.string.terms_of_service,
            icon = MoreIcon.ImageVectorIcon(Icons.Default.Info),
            event = MoreUiEvent.ON_TERMS_CLICK,
        ),
        MoreMenuItemPresentationModel(
            name = Res.string.privacy_policy,
            icon = MoreIcon.ImageVectorIcon(Icons.Default.Shield),
            event = MoreUiEvent.ON_PRIVACY_CLICK,
        ),
    )
    val premiumItemOption = MoreMenuItemPresentationModel(
        name = Res.string.become_premium,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Star),
        event = MoreUiEvent.ON_BECOME_PREMIUM_CLICK,
    )
    val instagramOption = MoreMenuItemPresentationModel(
        name = Res.string.instagram,
        icon = MoreIcon.DrawableResourceIcon(Res.drawable.ic_instagram),
        event = MoreUiEvent.ON_INSTAGRAM_CLICK,
    )
}
