package com.quare.bibleplanner.feature.read.screenshots

import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningWordTargetModel
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.read.fixture.NoDayCompletionBanner
import com.quare.bibleplanner.feature.read.fixture.availableListeningUiState
import com.quare.bibleplanner.feature.read.fixture.listeningPlayer
import com.quare.bibleplanner.feature.read.fixture.readUiState
import com.quare.bibleplanner.feature.read.presentation.screen.ReadNarrowScreen
import com.quare.bibleplanner.ui.screenshots.testing.ScreenshotTest
import org.junit.Test

internal class ReadListeningScreenshotTest : ScreenshotTest() {
    @Test
    fun followingTheWord() {
        snapshot(name = "following_the_word") {
            ReadNarrowScreen(
                platform = Platform.Android,
                state = readUiState(locale = "en-US"),
                listening = availableListeningUiState(
                    player = listeningPlayer(
                        verseNumber = 2,
                        verseText = "The earth was formless and empty.",
                        spokenWord = ListeningWordTargetModel.Verse(14..21),
                    ),
                ),
                onEvent = {},
                onListeningEvent = {},
                dayCompletionBanner = NoDayCompletionBanner,
            )
        }
    }
}
