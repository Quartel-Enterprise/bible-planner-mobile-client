package com.quare.bibleplanner.feature.more.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import bibleplanner.feature.more.generated.resources.Res
import bibleplanner.feature.more.generated.resources.become_pro
import bibleplanner.feature.more.generated.resources.delete_progress_option
import bibleplanner.feature.more.generated.resources.ic_instagram
import bibleplanner.feature.more.generated.resources.instagram
import bibleplanner.feature.more.generated.resources.release_notes_option
import bibleplanner.feature.more.generated.resources.release_notes_subtitle
import bibleplanner.feature.more.generated.resources.start_date
import bibleplanner.feature.more.generated.resources.theme_option
import bibleplanner.feature.more.generated.resources.web_app
import bibleplanner.feature.more.generated.resources.web_app_subtitle
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
    val webApp = MoreMenuItemPresentationModel(
        name = Res.string.web_app,
        subtitle = Res.string.web_app_subtitle,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Language),
        type = MoreOptionItemType.WEB_APP,
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
    val releaseNotes = MoreMenuItemPresentationModel(
        name = Res.string.release_notes_option,
        subtitle = Res.string.release_notes_subtitle,
        icon = MoreIcon.ImageVectorIcon(Icons.Default.Description),
        type = MoreOptionItemType.RELEASE_NOTES,
    )
}
