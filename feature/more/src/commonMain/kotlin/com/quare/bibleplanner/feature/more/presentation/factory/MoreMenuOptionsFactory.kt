package com.quare.bibleplanner.feature.more.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_pro
import bibleplanner.feature.more.generated.resources.delete_progress_option
import bibleplanner.feature.more.generated.resources.ic_instagram
import bibleplanner.feature.more.generated.resources.instagram
import bibleplanner.feature.more.generated.resources.privacy_policy
import bibleplanner.feature.more.generated.resources.start_date
import bibleplanner.feature.more.generated.resources.terms_of_service
import bibleplanner.feature.more.generated.resources.theme_option
import com.quare.bibleplanner.feature.more.presentation.model.MoreIcon
import com.quare.bibleplanner.feature.more.presentation.model.MoreMenuItemPresentationModel
import com.quare.bibleplanner.feature.more.presentation.model.MoreOptionItemType

internal object MoreMenuOptionsFactory {
    val pro = MoreMenuItemPresentationModel(
        name = Res.string.become_pro,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Star),
        type = MoreOptionItemType.BECOME_PRO,
    )
    val theme = MoreMenuItemPresentationModel(
        name = Res.string.theme_option,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Palette),
        type = MoreOptionItemType.THEME,
    )
    val instagram = MoreMenuItemPresentationModel(
        name = Res.string.instagram,
        icon = MoreIcon.DrawableResourceIcon(Res.drawable.ic_instagram),
        type = MoreOptionItemType.INSTAGRAM,
    )
    val privacyPolicy = MoreMenuItemPresentationModel(
        name = Res.string.privacy_policy,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Shield),
        type = MoreOptionItemType.PRIVACY_POLICY,
    )
    val termsOfService = MoreMenuItemPresentationModel(
        name = Res.string.terms_of_service,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Info),
        type = MoreOptionItemType.TERMS,
    )
    val deleteProgress = MoreMenuItemPresentationModel(
        name = Res.string.delete_progress_option,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Delete),
        type = MoreOptionItemType.DELETE_PROGRESS,
    )
    val editStartDate = MoreMenuItemPresentationModel(
        name = Res.string.start_date,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.EditCalendar),
        type = MoreOptionItemType.EDIT_PLAN_START_DAY,
    )
}
