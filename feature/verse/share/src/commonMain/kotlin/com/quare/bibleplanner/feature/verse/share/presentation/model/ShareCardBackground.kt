package com.quare.bibleplanner.feature.verse.share.presentation.model

import androidx.compose.ui.graphics.Color

/** The backgrounds offered for the share card, each with the text colours that stay legible on it. */
enum class ShareCardBackground(
    val startColor: Color,
    val endColor: Color,
    val textColor: Color,
    val subtitleColor: Color,
) {
    VIOLET(
        startColor = Color(0xFF4D5C96),
        endColor = Color(0xFFA4699E),
        textColor = Color(0xFFFFFFFF),
        subtitleColor = Color(0xB8FFFFFF),
    ),
    MIDNIGHT(
        startColor = Color(0xFF181F38),
        endColor = Color(0xFF0C0E1A),
        textColor = Color(0xFFE8E9F4),
        subtitleColor = Color(0x99E8E9F4),
    ),
    PARCHMENT(
        startColor = Color(0xFFF7EDDE),
        endColor = Color(0xFFE9D5BD),
        textColor = Color(0xFF3A2F22),
        subtitleColor = Color(0xA63A2F22),
    ),
    FOREST(
        startColor = Color(0xFF205141),
        endColor = Color(0xFF122B21),
        textColor = Color(0xFFE6F1EA),
        subtitleColor = Color(0xA6E6F1EA),
    ),
    WINE(
        startColor = Color(0xFF7D3648),
        endColor = Color(0xFF3D1A2A),
        textColor = Color(0xFFF6E7EC),
        subtitleColor = Color(0xA6F6E7EC),
    ),
    SUNSET(
        startColor = Color(0xFFE08A5A),
        endColor = Color(0xFFA94A5F),
        textColor = Color(0xFFFFFFFF),
        subtitleColor = Color(0xB8FFFFFF),
    ),
    SKY(
        startColor = Color(0xFF7FA8D9),
        endColor = Color(0xFF44548C),
        textColor = Color(0xFFFFFFFF),
        subtitleColor = Color(0xB8FFFFFF),
    ),
    GRAPHITE(
        startColor = Color(0xFF2B2B32),
        endColor = Color(0xFF131317),
        textColor = Color(0xFFECECF2),
        subtitleColor = Color(0x99ECECF2),
    ),
}
