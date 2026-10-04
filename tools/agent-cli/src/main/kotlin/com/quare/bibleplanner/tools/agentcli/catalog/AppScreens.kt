package com.quare.bibleplanner.tools.agentcli.catalog

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute

object AppScreens {
    // Why: the routes whose entry shows ViewModels that neither take the route nor are named after it.
    val viewModelsByRoute: Map<String, List<String>> = mapOf(
        "MainNavRoute" to listOf("MainScreenViewModel", "ReadingPlanViewModel", "BooksViewModel", "ProfileViewModel"),
        "Plans" to listOf("ReadingPlanViewModel"),
        "Books" to listOf("BooksViewModel"),
        "Profile" to listOf("ProfileViewModel"),
        "DayNavRoute" to listOf("DayViewModel", "DayStudyViewModel"),
        "MaterialYouBottomSheetNavRoute" to listOf("AndroidColorSchemeViewModel"),
        "BibleVersionSelectorRoute" to listOf("BibleVersionViewModel"),
        "ThemeNavRoute" to listOf("ThemeSelectionViewModel"),
        "EditPhotoSourceNavRoute" to listOf("ProfilePhotoViewModel"),
        "ExpandedPhotoNavRoute" to listOf("ProfilePhotoViewModel"),
        "ShareVerseImageNavRoute" to listOf("ShareVerseViewModel"),
    )

    // Why: the routes whose entry hands its ViewModels another route built from its own.
    val routeParameters: Map<String, (NavKey) -> NavKey> = mapOf(
        "ShareVerseImageNavRoute" to { route ->
            val imageRoute = route as ShareVerseImageNavRoute
            ShareVerseNavRoute(
                bookId = imageRoute.bookId,
                chapterNumber = imageRoute.chapterNumber,
                verseNumbers = imageRoute.verseNumbers,
            )
        },
    )

    // Why: the ViewModels RootAppNavDisplay and AppRoot keep alive above every screen.
    val appViewModels: List<String> = listOf(
        "AppViewModel",
        "DayStudyPanelViewModel",
        "DayStudyBackgroundGenerationViewModel",
        "InAppUpdateDownloadViewModel",
        "PendingBibleUpdatesPromptViewModel",
    )
}
