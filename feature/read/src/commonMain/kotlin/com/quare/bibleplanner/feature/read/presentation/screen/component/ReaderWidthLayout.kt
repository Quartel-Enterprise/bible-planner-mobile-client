package com.quare.bibleplanner.feature.read.presentation.screen.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.ui.utils.LocalIsWideLayout

private val wideReaderMinWidth = 520.dp

@Composable
internal fun ReaderWidthLayout(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val isWide = LocalIsWideLayout.current && maxWidth >= wideReaderMinWidth
        CompositionLocalProvider(LocalIsWideLayout provides isWide) {
            content()
        }
    }
}
