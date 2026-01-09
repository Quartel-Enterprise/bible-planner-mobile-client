package com.quare.bibleplanner.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

fun String.highlightText(
    query: String,
    highlightStyle: SpanStyle,
): AnnotatedString = if (query.isBlank()) {
    AnnotatedString(this)
} else {
    buildAnnotatedString {
        val normalizedText = this@highlightText.removeAccents()
        val normalizedQuery = query.removeAccents()
        val index = normalizedText.indexOf(
            string = normalizedQuery,
            startIndex = 0,
            ignoreCase = true,
        )

        if (index != -1) {
            append(
                substring(
                    startIndex = 0,
                    endIndex = index,
                ),
            )
            withStyle(highlightStyle) {
                append(
                    substring(
                        startIndex = index,
                        endIndex = index + query.length,
                    ),
                )
            }
            append(substring(startIndex = index + query.length))
        } else {
            append(this@highlightText)
        }
    }
}
