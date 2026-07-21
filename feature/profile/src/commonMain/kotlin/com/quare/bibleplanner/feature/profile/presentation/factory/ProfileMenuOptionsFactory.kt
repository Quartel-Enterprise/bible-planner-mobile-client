package com.quare.bibleplanner.feature.profile.presentation.factory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.app_language_option
import bibleplanner.feature.profile.generated.resources.become_pro
import bibleplanner.feature.profile.generated.resources.bible_version_option
import bibleplanner.feature.profile.generated.resources.check_for_updates_option
import bibleplanner.feature.profile.generated.resources.contact_support_option
import bibleplanner.feature.profile.generated.resources.delete_progress_option
import bibleplanner.feature.profile.generated.resources.ic_instagram
import bibleplanner.feature.profile.generated.resources.instagram
import bibleplanner.feature.profile.generated.resources.rate_app_option
import bibleplanner.feature.profile.generated.resources.release_notes_option
import bibleplanner.feature.profile.generated.resources.release_notes_subtitle
import bibleplanner.feature.profile.generated.resources.start_date
import bibleplanner.feature.profile.generated.resources.theme_option
import bibleplanner.feature.profile.generated.resources.web_app
import bibleplanner.feature.profile.generated.resources.web_app_subtitle
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileIcon
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileMenuItemPresentationModel
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType

internal object ProfileMenuOptionsFactory {
    val pro = ProfileMenuItemPresentationModel(
        name = Res.string.become_pro,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Star),
        type = ProfileOptionItemType.BECOME_PRO,
    )
    val theme = ProfileMenuItemPresentationModel(
        name = Res.string.theme_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Palette),
        type = ProfileOptionItemType.THEME,
    )
    val appLanguage = ProfileMenuItemPresentationModel(
        name = Res.string.app_language_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Translate),
        type = ProfileOptionItemType.APP_LANGUAGE,
    )
    val instagram = ProfileMenuItemPresentationModel(
        name = Res.string.instagram,
        icon = ProfileIcon.DrawableResourceIcon(Res.drawable.ic_instagram),
        type = ProfileOptionItemType.INSTAGRAM,
    )
    val webApp = ProfileMenuItemPresentationModel(
        name = Res.string.web_app,
        subtitle = Res.string.web_app_subtitle,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Language),
        type = ProfileOptionItemType.WEB_APP,
    )
    val deleteProgress = ProfileMenuItemPresentationModel(
        name = Res.string.delete_progress_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Delete),
        type = ProfileOptionItemType.DELETE_PROGRESS,
    )
    val editStartDate = ProfileMenuItemPresentationModel(
        name = Res.string.start_date,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.EditCalendar),
        type = ProfileOptionItemType.EDIT_PLAN_START_DAY,
    )
    val releaseNotes = ProfileMenuItemPresentationModel(
        name = Res.string.release_notes_option,
        subtitle = Res.string.release_notes_subtitle,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Description),
        type = ProfileOptionItemType.RELEASE_NOTES,
    )
    val bibleVersion = ProfileMenuItemPresentationModel(
        name = Res.string.bible_version_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.AutoMirrored.Filled.MenuBook),
        type = ProfileOptionItemType.BIBLE_VERSION,
    )
    val contactSupport = ProfileMenuItemPresentationModel(
        name = Res.string.contact_support_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.SupportAgent),
        type = ProfileOptionItemType.CONTACT_SUPPORT,
    )
    val rateApp = ProfileMenuItemPresentationModel(
        name = Res.string.rate_app_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.RateReview),
        type = ProfileOptionItemType.RATE_APP,
    )
    val checkForUpdate = ProfileMenuItemPresentationModel(
        name = Res.string.check_for_updates_option,
        icon = ProfileIcon.ImageVectorIcon(Icons.Default.Update),
        type = ProfileOptionItemType.CHECK_FOR_UPDATE,
    )
}
