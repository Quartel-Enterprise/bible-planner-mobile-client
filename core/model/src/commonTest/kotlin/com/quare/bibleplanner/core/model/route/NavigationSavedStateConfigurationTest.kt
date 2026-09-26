package com.quare.bibleplanner.core.model.route

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.PolymorphicSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NavigationSavedStateConfigurationTest {
    private val routes: List<NavKey> = listOf(
        MainNavRoute,
        MainNavRouteDestination.Plans,
        MainNavRouteDestination.Books,
        MainNavRouteDestination.Profile,
        AccountDetailsNavRoute,
        AddNotesFreeWarningNavRoute(maxFreeNotesAmount = 3),
        AppLanguageNavRoute,
        BibleVersionSelectorRoute,
        BookDetailsNavRoute(bookId = "GEN"),
        ChatNavRoute(
            source = ChatEntrySource.DAY_FAB,
            dayNumber = 2,
            weekNumber = 3,
            readingPlanType = "BOOKS",
        ),
        CongratsNavRoute,
        ContactSupportNavRoute,
        DayNavRoute(
            dayNumber = 1,
            weekNumber = 1,
            readingPlanType = "CHRONOLOGICAL",
        ),
        DayReadingCompleteNavRoute(
            dayNumber = 4,
            weekNumber = 5,
            readingPlanType = "BOOKS",
        ),
        DayStudyNavRoute(
            dayNumber = 6,
            weekNumber = 7,
            readingPlanType = "BOOKS",
        ),
        DeleteAccountNavRoute,
        DeleteAllProgressNavRoute,
        DeleteNotesRoute(
            readingPlanType = "BOOKS",
            week = 2,
            day = 3,
        ),
        DeleteVersionNavRoute(versionId = "KJV"),
        DonationNavRoute,
        EditNameNavRoute,
        EditPhotoSourceNavRoute,
        EditPlanStartDateNavRoute,
        EditProfileNavRoute,
        ExpandedPhotoNavRoute,
        InAppUpdateNavRoute(
            versionName = "2.8.0",
            source = "banner",
        ),
        LoginNavRoute(notifyResultViaSnackbar = true),
        LoginSyncNudgeNavRoute,
        LoginWarningNavRoute(reason = "purchase"),
        LogoutNavRoute,
        MaterialYouBottomSheetNavRoute,
        NotificationPermissionNavRoute,
        PaywallNavRoute(source = PaywallEntrySource.CHAT),
        PaywallTeaserNavRoute(reason = PaywallTeaserReason.HIGHLIGHT_CUSTOM_COLOR),
        PendingBibleUpdatesNavRoute,
        PixQrNavRoute,
        ReadNavRoute(
            bookId = "JHN",
            chapterNumber = 3,
            isChapterRead = false,
            isFromBookDetails = true,
        ),
        ReaderAppearanceNavRoute,
        DeleteHighlightColorNavRoute(colorKey = "yellow"),
        VerseNoteNavRoute(
            bibleVersionId = "WEB",
            bookId = "JHN",
            chapterNumber = 3,
            verseNumbers = listOf(16, 17),
            noteId = null,
        ),
        VerseSelectionNavRoute,
        ShareVerseNavRoute(
            bookId = "PSA",
            chapterNumber = 23,
            verseNumbers = listOf(1),
        ),
        ShareVerseImageNavRoute(
            bookId = "PSA",
            chapterNumber = 23,
            verseNumbers = listOf(1, 2),
        ),
        RenameDeviceNavRoute(
            deviceRowId = "row-1",
            currentName = "Pixel",
        ),
        ReleaseNotesNavRoute,
        StudySuggestionNavRoute,
        SubscriptionDetailsNavRoute,
        ThemeNavRoute,
        UpdateDownloadedNavRoute,
    )

    @Test
    fun `GIVEN every registered route WHEN saving and restoring it THEN restores an equal route`() {
        // Given
        val serializer = PolymorphicSerializer(NavKey::class)

        // When
        val restored = routes.map { route ->
            decodeFromSavedState(
                deserializer = serializer,
                savedState = encodeToSavedState(
                    serializer = serializer,
                    value = route,
                    configuration = navigationSavedStateConfiguration,
                ),
                configuration = navigationSavedStateConfiguration,
            )
        }

        // Then
        assertEquals(routes, restored)
    }
}
