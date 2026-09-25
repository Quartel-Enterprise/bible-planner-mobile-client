package com.quare.bibleplanner.ui.icons

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.LayoutDirection
import platform.UIKit.UIImage
import kotlin.math.roundToInt

internal class SfSymbolPainter(
    private val symbolName: String,
    private val symbol: UIImage,
) : Painter() {
    private var colorFilter: ColorFilter? = null

    override val intrinsicSize: Size = Size.Unspecified

    override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
        this.colorFilter = colorFilter
        return true
    }

    override fun DrawScope.onDraw() {
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        if (width <= 0 || height <= 0) return
        val bitmap = SfSymbolRenderer.getBitmap(
            symbolName = symbolName,
            symbol = symbol,
            width = width,
            height = height,
        )
        if (layoutDirection == LayoutDirection.Rtl && symbol.flipsForRightToLeftLayoutDirection) {
            scale(scaleX = -1f, scaleY = 1f) {
                drawImage(
                    image = bitmap,
                    colorFilter = colorFilter,
                )
            }
        } else {
            drawImage(
                image = bitmap,
                colorFilter = colorFilter,
            )
        }
    }
}
