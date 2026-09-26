package com.quare.bibleplanner.feature.chat.fixture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.quare.bibleplanner.core.model.theme.Theme
import com.quare.bibleplanner.feature.chat.presentation.ChatScreen
import com.quare.bibleplanner.ui.theme.AppTheme
import com.quare.bibleplanner.ui.theme.model.LocalTheme
import kotlinx.coroutines.flow.emptyFlow

internal const val CHAT_SCREENSHOT = "09_chat"

/**
 * The chat as the store screenshots show it, on every platform that renders them: the Robolectric
 * generators for Play and the iOS simulator captures for the App Store.
 *
 * [statusBarHeight] is the room the frame's status bar takes at the top. The Play frames reserve
 * it themselves, so they pass zero; an iOS capture fills the whole screen and passes its slot's.
 */
@Composable
internal fun ChatScreenshotContent(
    locale: String,
    statusBarHeight: Dp,
) {
    CompositionLocalProvider(LocalTheme provides Theme.DARK) {
        AppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(modifier = Modifier.padding(top = statusBarHeight)) {
                    ChatScreen(
                        uiState = chatUiState(locale),
                        scrollToBottomRequests = emptyFlow(),
                        onEvent = {},
                        onNavigateBack = {},
                    )
                }
            }
        }
    }
}
