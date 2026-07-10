package com.quare.bibleplanner.core.provider.analytics.domain.mapper

import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.AppLanguageNavRoute
import com.quare.bibleplanner.core.model.route.BibleVersionSelectorRoute
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.model.route.CongratsNavRoute
import com.quare.bibleplanner.core.model.route.ContactSupportNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DeleteAllProgressNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.model.route.DonationNavRoute
import com.quare.bibleplanner.core.model.route.EditPlanStartDateNavRoute
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
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.Destination
import com.quare.bibleplanner.core.provider.analytics.domain.model.DestinationType

internal class NavRouteToDestinationMapperImpl : NavRouteToDestinationMapper {
    override fun map(route: NavRoute): Destination? = when (route) {
        is MainNavRoute -> null

        is MainNavRouteDestination.Plans -> screen("plans")

        is MainNavRouteDestination.Books -> screen("books")

        is MainNavRouteDestination.More -> screen("more")

        is AddNotesFreeWarningNavRoute -> dialog(
            name = "add_notes_free_warning",
            params = mapOf(AnalyticsParams.MAX_FREE_NOTES to route.maxFreeNotesAmount),
        )

        is AppLanguageNavRoute -> responsive("app_language")

        is BibleVersionSelectorRoute -> responsive("bible_version_selector")

        is BookDetailsNavRoute -> screen(
            name = "book_details",
            params = mapOf(AnalyticsParams.BOOK_ID to route.bookId),
        )

        is CongratsNavRoute -> bottomSheet("congrats")

        is ContactSupportNavRoute -> responsive("contact_support")

        is DayNavRoute -> screen(
            name = "day",
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType,
                AnalyticsParams.WEEK_NUMBER to route.weekNumber,
                AnalyticsParams.DAY_NUMBER to route.dayNumber,
            ),
        )

        is DeleteAllProgressNavRoute -> dialog("delete_all_progress")

        is DeleteNotesRoute -> dialog(
            name = "delete_notes",
            params = mapOf(
                AnalyticsParams.PLAN_TYPE to route.readingPlanType,
                AnalyticsParams.WEEK_NUMBER to route.week,
                AnalyticsParams.DAY_NUMBER to route.day,
            ),
        )

        is DeleteVersionNavRoute -> dialog(
            name = "delete_version",
            params = mapOf(AnalyticsParams.VERSION_ID to route.versionId),
        )

        is DonationNavRoute -> bottomSheet("donation")

        is EditPlanStartDateNavRoute -> dialog("edit_plan_start_date")

        is LoginNavRoute -> bottomSheet("login")

        is LoginSyncNudgeNavRoute -> dialog("login_sync_nudge")

        is LoginWarningNavRoute -> dialog(
            name = "login_warning",
            params = mapOf(AnalyticsParams.REASON to route.reason),
        )

        is LogoutNavRoute -> dialog("logout")

        is MaterialYouBottomSheetNavRoute -> dialog("material_you")

        is NotificationPermissionNavRoute -> dialog("notification_permission")

        is PaywallNavRoute -> screen("paywall")

        is PixQrNavRoute -> dialog("pix_qr")

        is ReadNavRoute -> screen(
            name = "read",
            params = mapOf(
                AnalyticsParams.BOOK_ID to route.bookId,
                AnalyticsParams.CHAPTER_NUMBER to route.chapterNumber,
            ),
        )

        is ReleaseNotesNavRoute -> screen("release_notes")

        is SubscriptionDetailsNavRoute -> dialog("subscription_details")

        is ThemeNavRoute -> responsive("theme_selection")
    }

    private fun screen(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.SCREEN,
        params = params,
    )

    private fun dialog(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.DIALOG,
        params = params,
    )

    private fun bottomSheet(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.BOTTOM_SHEET,
        params = params,
    )

    private fun responsive(
        name: String,
        params: Map<String, Any> = emptyMap(),
    ): Destination = Destination(
        name = name,
        type = DestinationType.RESPONSIVE,
        params = params,
    )
}
