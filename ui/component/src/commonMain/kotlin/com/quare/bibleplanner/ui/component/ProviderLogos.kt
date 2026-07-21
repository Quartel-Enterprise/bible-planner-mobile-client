package com.quare.bibleplanner.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Brand marks are not part of the Material icon set, so they are rebuilt here from the SVG paths in
// the design. The Google mark keeps its four brand colors (render with Image); the Apple mark is
// monochrome (render with Icon so it takes the content color).

private val logoSize = 24.dp
private const val VIEWPORT = 24f

private const val GOOGLE_BLUE_PATH = "M22.5 12.2c0-.7-.06-1.4-.18-2.06H12v3.9h5.9a5.04 5.04 0 0 1-2.19 " +
    "3.3v2.74h3.54c2.07-1.9 3.25-4.71 3.25-8.05z"
private const val GOOGLE_GREEN_PATH = "M12 23c2.94 0 5.42-.97 7.22-2.63l-3.54-2.74c-.98.66-2.24 1.05-3.68 " +
    "1.05-2.83 0-5.23-1.91-6.09-4.48H2.26v2.82A10.98 10.98 0 0 0 12 23z"
private const val GOOGLE_YELLOW_PATH = "M5.91 14.2a6.6 6.6 0 0 1 0-4.2V7.18H2.26a11 11 0 0 0 0 9.84l3.65-2.82z"
private const val GOOGLE_RED_PATH = "M12 5.32c1.6 0 3.03.55 4.16 1.62l3.12-3.12A10.98 10.98 0 0 0 12 1 10.98 " +
    "10.98 0 0 0 2.26 7.18l3.65 2.82C6.77 7.23 9.17 5.32 12 5.32z"
private const val APPLE_PATH = "M16.36 12.9c.02 2.6 2.28 3.46 2.3 3.47-.02.06-.36 1.24-1.19 2.46-.72 1.05-1.47 " +
    "2.1-2.65 2.12-1.16.02-1.53-.69-2.85-.69-1.32 0-1.73.67-2.83.71-1.14.04-2-.14-3.44-2.24C3.03 16.42 1.9 12.9 " +
    "3.35 10.53c.72-1.19 2-1.94 3.39-1.96 1.12-.02 2.17.75 2.85.75.68 0 1.96-.93 3.3-.79.56.02 2.14.23 3.15 " +
    "1.71-.08.05-1.88 1.1-1.68 3.66zM14.2 7.34c.6-.73 1.01-1.75.9-2.76-.87.04-1.92.58-2.54 1.31-.56.65-1.05 " +
    "1.68-.92 2.67.97.08 1.96-.49 2.56-1.22z"

@Composable
fun googleLogo(): ImageVector = remember {
    logoBuilder(name = "GoogleLogo")
        .apply {
            addColoredPath(color = Color(0xFF4285F4), pathData = GOOGLE_BLUE_PATH)
            addColoredPath(color = Color(0xFF34A853), pathData = GOOGLE_GREEN_PATH)
            addColoredPath(color = Color(0xFFFBBC05), pathData = GOOGLE_YELLOW_PATH)
            addColoredPath(color = Color(0xFFEA4335), pathData = GOOGLE_RED_PATH)
        }.build()
}

@Composable
fun appleLogo(): ImageVector = remember {
    logoBuilder(name = "AppleLogo")
        .apply {
            addColoredPath(color = Color.Black, pathData = APPLE_PATH)
        }.build()
}

private fun logoBuilder(name: String): ImageVector.Builder = ImageVector.Builder(
    name = name,
    defaultWidth = logoSize,
    defaultHeight = logoSize,
    viewportWidth = VIEWPORT,
    viewportHeight = VIEWPORT,
)

private fun ImageVector.Builder.addColoredPath(
    color: Color,
    pathData: String,
) {
    addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(color),
    )
}
