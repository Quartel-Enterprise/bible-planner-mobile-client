package com.quare.bibleplanner.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
actual fun rememberAppIconPainter(icon: AppIcon): Painter {
    val symbolPainter = remember(icon) {
        SfSymbolRenderer.loadSymbol(icon.sfSymbol)?.let { symbol ->
            SfSymbolPainter(
                symbolName = icon.sfSymbol,
                symbol = symbol,
            )
        }
    }
    return symbolPainter ?: rememberVectorPainter(icon.material())
}
