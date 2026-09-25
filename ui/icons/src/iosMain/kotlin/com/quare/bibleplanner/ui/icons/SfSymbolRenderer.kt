package com.quare.bibleplanner.ui.icons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UIImageSymbolConfiguration
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal object SfSymbolRenderer {
    private val cache = mutableMapOf<SfSymbolBitmapKey, ImageBitmap>()
    private val configuration = UIImageSymbolConfiguration.configurationWithPointSize(REFERENCE_POINT_SIZE)

    fun loadSymbol(name: String): UIImage? = UIImage.systemImageNamed(
        name = name,
        withConfiguration = configuration,
    )

    fun getBitmap(
        symbolName: String,
        symbol: UIImage,
        width: Int,
        height: Int,
    ): ImageBitmap = cache.getOrPut(
        SfSymbolBitmapKey(
            symbolName = symbolName,
            width = width,
            height = height,
        ),
    ) {
        render(
            symbol = symbol,
            width = width.toDouble(),
            height = height.toDouble(),
        )
    }

    private fun render(
        symbol: UIImage,
        width: Double,
        height: Double,
    ): ImageBitmap {
        val (symbolWidth, symbolHeight) = symbol.size.useContents { this.width to this.height }
        val scale = minOf(
            height * POINT_SIZE_TO_BOX_RATIO / REFERENCE_POINT_SIZE,
            width / symbolWidth,
            height / symbolHeight,
        )
        val drawWidth = symbolWidth * scale
        val drawHeight = symbolHeight * scale
        val format = UIGraphicsImageRendererFormat.preferredFormat().apply {
            setScale(1.0)
            setOpaque(false)
        }
        val renderer = UIGraphicsImageRenderer(
            size = CGSizeMake(width, height),
            format = format,
        )
        val png = renderer.PNGDataWithActions { _ ->
            symbol
                .imageWithTintColor(UIColor.blackColor, UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
                .drawInRect(
                    CGRectMake(
                        x = (width - drawWidth) / 2,
                        y = (height - drawHeight) / 2,
                        width = drawWidth,
                        height = drawHeight,
                    ),
                )
        }
        return Image.makeFromEncoded(png.toByteArray()).toComposeImageBitmap()
    }

    private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
    }

    private const val REFERENCE_POINT_SIZE = 17.0
    private const val POINT_SIZE_TO_BOX_RATIO = 0.75
}
