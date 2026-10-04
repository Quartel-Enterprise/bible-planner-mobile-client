package com.quare.bibleplanner.ui.screenshots.testing

import com.quare.bibleplanner.core.model.theme.Theme

enum class ScreenshotVariant(
    val theme: Theme,
    val languageTag: String,
    val fontScale: Float,
    val fileSuffix: String,
) {
    LIGHT(
        theme = Theme.LIGHT,
        languageTag = "en-US",
        fontScale = 1f,
        fileSuffix = "light",
    ),
    DARK(
        theme = Theme.DARK,
        languageTag = "en-US",
        fontScale = 1f,
        fileSuffix = "dark",
    ),
    LARGE_FONT_PT_BR(
        theme = Theme.DARK,
        languageTag = "pt-BR",
        fontScale = 1.5f,
        fileSuffix = "pt-BR_large-font",
    ),
    LARGEST_FONT(
        theme = Theme.LIGHT,
        languageTag = "en-US",
        fontScale = 2f,
        fileSuffix = "largest-font",
    ),
    ;

    companion object {
        val themes: List<ScreenshotVariant> = listOf(LIGHT, DARK)
        val all: List<ScreenshotVariant> = entries
    }
}
