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
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.ReleaseNotesNavRoute
import com.quare.bibleplanner.core.model.route.SubscriptionDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ThemeNavRoute
import com.quare.bibleplanner.core.model.route.UpdateDownloadedNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.DestinationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavRouteToDestinationMapperImplTest {
    private val mapper = NavRouteToDestinationMapperImpl()

    @Test
    fun `GIVEN MainNavRoute WHEN mapping THEN returns null`() {
        assertNull(mapper.map(MainNavRoute))
    }

    @Test
    fun `GIVEN every other route WHEN mapping THEN returns the expected destination_name and destination_type`() {
        val expectations: List<Pair<NavRoute, Pair<String, DestinationType>>> = listOf(
            MainNavRouteDestination.Plans to ("plans" to DestinationType.SCREEN),
            MainNavRouteDestination.Books to ("books" to DestinationType.SCREEN),
            MainNavRouteDestination.More to ("more" to DestinationType.SCREEN),
            AddNotesFreeWarningNavRoute(maxFreeNotesAmount = 3) to
                ("add_notes_free_warning" to DestinationType.DIALOG),
            AppLanguageNavRoute to ("app_language" to DestinationType.RESPONSIVE),
            BibleVersionSelectorRoute to ("bible_version_selector" to DestinationType.RESPONSIVE),
            BookDetailsNavRoute(bookId = "genesis") to ("book_details" to DestinationType.SCREEN),
            CongratsNavRoute to ("congrats" to DestinationType.BOTTOM_SHEET),
            ContactSupportNavRoute to ("contact_support" to DestinationType.RESPONSIVE),
            DayNavRoute(dayNumber = 1, weekNumber = 2, readingPlanType = "chronological") to
                ("day" to DestinationType.SCREEN),
            DeleteAllProgressNavRoute to ("delete_all_progress" to DestinationType.DIALOG),
            DeleteNotesRoute(readingPlanType = "books", week = 1, day = 2) to
                ("delete_notes" to DestinationType.DIALOG),
            DeleteVersionNavRoute(versionId = "kjv") to ("delete_version" to DestinationType.DIALOG),
            DonationNavRoute to ("donation" to DestinationType.BOTTOM_SHEET),
            EditPlanStartDateNavRoute to ("edit_plan_start_date" to DestinationType.DIALOG),
            InAppUpdateNavRoute(versionName = "2.0.0", source = "startup") to
                ("in_app_update" to DestinationType.RESPONSIVE),
            LoginNavRoute(notifyResultViaSnackbar = true) to ("login" to DestinationType.BOTTOM_SHEET),
            LoginSyncNudgeNavRoute to ("login_sync_nudge" to DestinationType.DIALOG),
            LoginWarningNavRoute(reason = "sync_setting") to ("login_warning" to DestinationType.DIALOG),
            LogoutNavRoute to ("logout" to DestinationType.DIALOG),
            MaterialYouBottomSheetNavRoute to ("material_you" to DestinationType.DIALOG),
            NotificationPermissionNavRoute to ("notification_permission" to DestinationType.DIALOG),
            PaywallNavRoute to ("paywall" to DestinationType.SCREEN),
            PixQrNavRoute to ("pix_qr" to DestinationType.DIALOG),
            ReadNavRoute(bookId = "exodus", chapterNumber = 4, isChapterRead = true, isFromBookDetails = false) to
                ("read" to DestinationType.SCREEN),
            ReleaseNotesNavRoute to ("release_notes" to DestinationType.SCREEN),
            SubscriptionDetailsNavRoute to ("subscription_details" to DestinationType.DIALOG),
            ThemeNavRoute to ("theme_selection" to DestinationType.RESPONSIVE),
            UpdateDownloadedNavRoute to ("update_downloaded" to DestinationType.DIALOG),
        )

        expectations.forEach { (route, expected) ->
            val (expectedName, expectedType) = expected
            val destination = mapper.map(route)
            assertEquals(expectedName, destination?.name, "destination_name mismatch for $route")
            assertEquals(expectedType, destination?.type, "destination_type mismatch for $route")
        }
    }

    @Test
    fun `GIVEN AddNotesFreeWarningNavRoute WHEN mapping THEN carries max_free_notes param`() {
        val destination = mapper.map(AddNotesFreeWarningNavRoute(maxFreeNotesAmount = 5))

        assertEquals(mapOf(AnalyticsParams.MAX_FREE_NOTES to 5), destination?.params)
    }

    @Test
    fun `GIVEN BookDetailsNavRoute WHEN mapping THEN carries book_id param`() {
        val destination = mapper.map(BookDetailsNavRoute(bookId = "genesis"))

        assertEquals(mapOf(AnalyticsParams.BOOK_ID to "genesis"), destination?.params)
    }

    @Test
    fun `GIVEN DayNavRoute WHEN mapping THEN carries plan_type week_number and day_number params`() {
        val destination = mapper.map(
            DayNavRoute(dayNumber = 3, weekNumber = 2, readingPlanType = "chronological"),
        )

        assertEquals(
            mapOf(
                AnalyticsParams.PLAN_TYPE to "chronological",
                AnalyticsParams.WEEK_NUMBER to 2,
                AnalyticsParams.DAY_NUMBER to 3,
            ),
            destination?.params,
        )
    }

    @Test
    fun `GIVEN DeleteNotesRoute WHEN mapping THEN carries plan_type week_number and day_number params`() {
        val destination = mapper.map(DeleteNotesRoute(readingPlanType = "books", week = 4, day = 5))

        assertEquals(
            mapOf(
                AnalyticsParams.PLAN_TYPE to "books",
                AnalyticsParams.WEEK_NUMBER to 4,
                AnalyticsParams.DAY_NUMBER to 5,
            ),
            destination?.params,
        )
    }

    @Test
    fun `GIVEN DeleteVersionNavRoute WHEN mapping THEN carries version_id param`() {
        val destination = mapper.map(DeleteVersionNavRoute(versionId = "kjv"))

        assertEquals(mapOf(AnalyticsParams.VERSION_ID to "kjv"), destination?.params)
    }

    @Test
    fun `GIVEN LoginWarningNavRoute WHEN mapping THEN carries reason param`() {
        val destination = mapper.map(LoginWarningNavRoute(reason = "sync_setting"))

        assertEquals(mapOf(AnalyticsParams.REASON to "sync_setting"), destination?.params)
    }

    @Test
    fun `GIVEN ReadNavRoute WHEN mapping THEN carries book_id and chapter_number params`() {
        val destination = mapper.map(
            ReadNavRoute(bookId = "exodus", chapterNumber = 4, isChapterRead = true, isFromBookDetails = false),
        )

        assertEquals(
            mapOf(
                AnalyticsParams.BOOK_ID to "exodus",
                AnalyticsParams.CHAPTER_NUMBER to 4,
            ),
            destination?.params,
        )
    }

    @Test
    fun `GIVEN a route with no args WHEN mapping THEN params are empty`() {
        val destination = mapper.map(LogoutNavRoute)

        assertEquals(emptyMap(), destination?.params)
    }
}
