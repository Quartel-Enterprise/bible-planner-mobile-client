package com.quare.bibleplanner.feature.profile.presentation.factory

import bibleplanner.feature.profile.generated.resources.Res
import bibleplanner.feature.profile.generated.resources.app_language_option
import bibleplanner.feature.profile.generated.resources.become_pro
import bibleplanner.feature.profile.generated.resources.bible_version_option
import bibleplanner.feature.profile.generated.resources.check_for_updates_option
import bibleplanner.feature.profile.generated.resources.contact_support_option
import bibleplanner.feature.profile.generated.resources.delete_account_option
import bibleplanner.feature.profile.generated.resources.delete_account_option_subtitle
import bibleplanner.feature.profile.generated.resources.delete_progress_option
import bibleplanner.feature.profile.generated.resources.delete_progress_option_subtitle
import bibleplanner.feature.profile.generated.resources.ic_instagram
import bibleplanner.feature.profile.generated.resources.instagram
import bibleplanner.feature.profile.generated.resources.rate_app_option
import bibleplanner.feature.profile.generated.resources.release_notes_option
import bibleplanner.feature.profile.generated.resources.release_notes_subtitle
import bibleplanner.feature.profile.generated.resources.start_date
import bibleplanner.feature.profile.generated.resources.study_suggestion_option
import bibleplanner.feature.profile.generated.resources.theme_option
import bibleplanner.feature.profile.generated.resources.web_app
import bibleplanner.feature.profile.generated.resources.web_app_subtitle
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileIcon
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileMenuItemPresentationModel
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import com.quare.bibleplanner.ui.icons.AppIcon

internal object ProfileMenuOptionsFactory {
    val pro = ProfileMenuItemPresentationModel(
        name = Res.string.become_pro,
        icon = ProfileIcon.SystemIcon(AppIcon.Star),
        type = ProfileOptionItemType.BECOME_PRO,
    )
    val theme = ProfileMenuItemPresentationModel(
        name = Res.string.theme_option,
        icon = ProfileIcon.SystemIcon(AppIcon.Palette),
        type = ProfileOptionItemType.THEME,
    )
    val appLanguage = ProfileMenuItemPresentationModel(
        name = Res.string.app_language_option,
        icon = ProfileIcon.SystemIcon(AppIcon.Translate),
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
        icon = ProfileIcon.SystemIcon(AppIcon.Language),
        type = ProfileOptionItemType.WEB_APP,
    )
    val deleteProgress = ProfileMenuItemPresentationModel(
        name = Res.string.delete_progress_option,
        subtitle = Res.string.delete_progress_option_subtitle,
        icon = ProfileIcon.SystemIcon(AppIcon.DeleteSweep),
        type = ProfileOptionItemType.DELETE_PROGRESS,
    )
    val deleteAccount = ProfileMenuItemPresentationModel(
        name = Res.string.delete_account_option,
        subtitle = Res.string.delete_account_option_subtitle,
        icon = ProfileIcon.SystemIcon(AppIcon.PersonRemove),
        type = ProfileOptionItemType.DELETE_ACCOUNT,
    )
    val studySuggestion = ProfileMenuItemPresentationModel(
        name = Res.string.study_suggestion_option,
        icon = ProfileIcon.SystemIcon(AppIcon.AutoAwesome),
        type = ProfileOptionItemType.STUDY_SUGGESTION,
    )
    val editStartDate = ProfileMenuItemPresentationModel(
        name = Res.string.start_date,
        icon = ProfileIcon.SystemIcon(AppIcon.EditCalendar),
        type = ProfileOptionItemType.EDIT_PLAN_START_DAY,
    )
    val releaseNotes = ProfileMenuItemPresentationModel(
        name = Res.string.release_notes_option,
        subtitle = Res.string.release_notes_subtitle,
        icon = ProfileIcon.SystemIcon(AppIcon.Description),
        type = ProfileOptionItemType.RELEASE_NOTES,
    )
    val bibleVersion = ProfileMenuItemPresentationModel(
        name = Res.string.bible_version_option,
        icon = ProfileIcon.SystemIcon(AppIcon.MenuBook),
        type = ProfileOptionItemType.BIBLE_VERSION,
    )
    val contactSupport = ProfileMenuItemPresentationModel(
        name = Res.string.contact_support_option,
        icon = ProfileIcon.SystemIcon(AppIcon.SupportAgent),
        type = ProfileOptionItemType.CONTACT_SUPPORT,
    )
    val rateApp = ProfileMenuItemPresentationModel(
        name = Res.string.rate_app_option,
        icon = ProfileIcon.SystemIcon(AppIcon.RateReview),
        type = ProfileOptionItemType.RATE_APP,
    )
    val checkForUpdate = ProfileMenuItemPresentationModel(
        name = Res.string.check_for_updates_option,
        icon = ProfileIcon.SystemIcon(AppIcon.Update),
        type = ProfileOptionItemType.CHECK_FOR_UPDATE,
    )
}
