package com.quare.bibleplanner.core.provider.analytics.domain.mapper

import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAccountNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditNameNavRoute
import com.quare.bibleplanner.core.model.route.EditPhotoSourceNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
import com.quare.bibleplanner.core.model.route.EditProfileNavRoute
import com.quare.bibleplanner.core.model.route.ExpandedPhotoNavRoute
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.model.route.LoginNavRoute
import com.quare.bibleplanner.core.model.route.LoginSyncNudgeNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.model.route.MaterialYouBottomSheetNavRoute
import com.quare.bibleplanner.core.model.route.NavRoute
import com.quare.bibleplanner.core.model.route.NotificationPermissionNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.PendingBibleUpdatesNavRoute
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReaderAppearanceNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.StudySuggestionNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.model.route.VerseSelectionNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.Destination
import com.quare.bibleplanner.core.provider.analytics.domain.model.DestinationType
import com.quare.bibleplanner.core.provider.analytics.domain.model.toPlanTypeAnalyticsValue

internal class NavRouteToDestinationMapperImpl : NavRouteToDestinationMapper {
    override fun map(route: NavRoute): Destination? = when (route) {
        is MainNavRoute -> null

        is MainNavRouteDestination.Plans -> createScreenDestination("plans")

        is MainNavRouteDestination.Books -> createScreenDestination("books")

        is MainNavRouteDestination.Profile -> createScreenDestination("profile")

        is AccountDetailsNavRoute -> createResponsiveDestination("account_details")

        is EditProfileNavRoute -> createResponsiveDestination("edit_profile")

        is CropPhotoNavRoute -> createScreenDestination("crop_profile_photo")

        is EditPhotoSourceNavRoute -> createResponsiveDestination("edit_profile_photo_source")

        is EditNameNavRoute -> createDialogDestination("edit_profile_name")

        is ExpandedPhotoNavRoute -> createDialogDestination("profile_photo_expanded")

        is AddNotesFreeWarningNavRoute -> createDialogDestination(
            name = "add_notes_free_warning",
            params = mapOf(AnalyticsParams.MAX_FREE_NOTES to route.maxFreeNotesAmount),
        )

        is AppLanguageNavRoute -> createResponsiveDestination("app_language")

        is BibleVersionSelectorRoute -> createResponsiveDestination("bible_version_selector")

        is BookDetailsNavRoute -> createScreenDestination(
            name = "book_details",
            params = mapOf(AnalyticsParams.BOOK_ID to route.bookId),
        )

        is CongratsNavRoute -> createBottomSheetDestination("congrats")

        is ContactSupportNavRoute -> createResponsiveDestination("contact_support")

        is DayNavRoute -> createScreenDestination(
            name = "day",
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType.toPlanTypeAnalyticsValue(),
                AnalyticsParams.WEEK_NUMBER to route.weekNumber,
                AnalyticsParams.DAY_NUMBER to route.dayNumber,
            ),
        )

        is DayStudyNavRoute -> createScreenDestination(
            name = "day_study",
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType.toPlanTypeAnalyticsValue(),
                AnalyticsParams.WEEK_NUMBER to route.weekNumber,
                AnalyticsParams.DAY_NUMBER to route.dayNumber,
            ),
        )

        is DayReadingCompleteNavRoute -> createResponsiveDestination(
            name = "day_reading_complete",
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType.toPlanTypeAnalyticsValue(),
                AnalyticsParams.WEEK_NUMBER to route.weekNumber,
                AnalyticsParams.DAY_NUMBER to route.dayNumber,
            ),
        )

        is ChatNavRoute -> createScreenDestination(
            name = "ai_chat",
            params = buildMap {
                put(AnalyticsParams.SOURCE, route.source.key)
                route.readingPlanType?.let { put(AnalyticsParams.PLAN_TYPE, it.toPlanTypeAnalyticsValue()) }
                route.weekNumber?.let { put(AnalyticsParams.WEEK_NUMBER, it) }
                route.dayNumber?.let { put(AnalyticsParams.DAY_NUMBER, it) }
            },
        )

        is DeleteAllProgressNavRoute -> createDialogDestination("delete_all_progress")

        is DeleteAccountNavRoute -> createDialogDestination("delete_account")

        is DeleteNotesRoute -> createDialogDestination(
            name = "delete_notes",
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType.toPlanTypeAnalyticsValue(),
                AnalyticsParams.WEEK_NUMBER to route.week,
                AnalyticsParams.DAY_NUMBER to route.day,
            ),
        )

        is DeleteVersionNavRoute -> createDialogDestination(
            name = "delete_version",
            params = mapOf(AnalyticsParams.VERSION_ID to route.versionId),
        )

        is DonationNavRoute -> createBottomSheetDestination("donation")

        is EditPlanStartDateNavRoute -> createDialogDestination("edit_plan_start_date")

        is InAppUpdateNavRoute -> createResponsiveDestination("in_app_update")

        is LoginNavRoute -> createBottomSheetDestination("login")

        is LoginSyncNudgeNavRoute -> createDialogDestination("login_sync_nudge")

        is LoginWarningNavRoute -> createDialogDestination(
            name = "login_warning",
            params = mapOf(AnalyticsParams.REASON to route.reason),
        )

        is LogoutNavRoute -> createDialogDestination("logout")

        is MaterialYouBottomSheetNavRoute -> createDialogDestination("material_you")

        is NotificationPermissionNavRoute -> createDialogDestination("notification_permission")

        is PaywallNavRoute -> createScreenDestination(
            name = "paywall",
            params = mapOf(AnalyticsParams.SOURCE to route.source.key),
        )

        is PaywallTeaserNavRoute -> createResponsiveDestination(
            name = "paywall_teaser",
            params = mapOf(AnalyticsParams.REASON to route.reason.key),
        )

        is PixQrNavRoute -> createDialogDestination("pix_qr")

        is ReadNavRoute -> createScreenDestination(
            name = "read",
            params = mapOf(
                AnalyticsParams.BOOK_ID to route.bookId,
                AnalyticsParams.CHAPTER_NUMBER to route.chapterNumber,
            ),
        )

        is ReaderAppearanceNavRoute -> createResponsiveDestination("reader_appearance")

        is DeleteHighlightColorNavRoute -> createDialogDestination("delete_highlight_color")

        is VerseNoteNavRoute -> createResponsiveDestination("verse_note")

        is VerseSelectionNavRoute -> createResponsiveDestination("verse_selection")

        is ShareVerseNavRoute -> createResponsiveDestination("share_verse")

        is ShareVerseImageNavRoute -> createResponsiveDestination("share_verse_image")

        is PendingBibleUpdatesNavRoute -> createDialogDestination("pending_bible_updates")

        is ReleaseNotesNavRoute -> createScreenDestination("release_notes")

        is RenameDeviceNavRoute -> createDialogDestination("rename_device")

        is StudySuggestionNavRoute -> createResponsiveDestination("study_suggestion")

        is SubscriptionDetailsNavRoute -> createDialogDestination("subscription_details")

        is ThemeNavRoute -> createResponsiveDestination("theme_selection")

        is UpdateDownloadedNavRoute -> createDialogDestination("update_downloaded")
    }

    private fun createScreenDestination(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.SCREEN,
        params = params,
    )

    private fun createDialogDestination(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.DIALOG,
        params = params,
    )

    private fun createBottomSheetDestination(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.BOTTOM_SHEET,
        params = params,
    )

    private fun createResponsiveDestination(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.RESPONSIVE,
        params = params,
    )
}
