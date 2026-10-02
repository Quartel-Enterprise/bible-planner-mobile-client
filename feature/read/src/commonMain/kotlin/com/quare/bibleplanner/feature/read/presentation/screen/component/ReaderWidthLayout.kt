package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout

/** The width the wide reader's header needs to fit its title and every action beside it. */
private val wideReaderMinWidth = 520.dp

/**
 * A wide window doesn't always leave the reader a wide pane: with the chapter study open beside it,
 * the reader gets only part of the width. Below what the wide header needs, the reader takes the
 * layout it has on a phone, which is built for that width.
 */
@Composable
internal fun ReaderWidthLayout(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val isWide = LocalIsWideLayout.current && maxWidth >= wideReaderMinWidth
        CompositionLocalProvider(LocalIsWideLayout provides isWide) {
            content()
        }
    }
}
