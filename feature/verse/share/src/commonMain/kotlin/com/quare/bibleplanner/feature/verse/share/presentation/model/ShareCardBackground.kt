package com.quare.bibleplanner.feature.verse.share.presentation.model

enum class ShareCardBackground(
    val startArgb: Long,
    val endArgb: Long,
    val textArgb: Long,
    val subtitleArgb: Long,
) {
    VIOLET(
        startArgb = 0xFF4D5C96,
        endArgb = 0xFFA4699E,
        textArgb = 0xFFFFFFFF,
        subtitleArgb = 0xB8FFFFFF,
    ),
    MIDNIGHT(
        startArgb = 0xFF181F38,
        endArgb = 0xFF0C0E1A,
        textArgb = 0xFFE8E9F4,
        subtitleArgb = 0x99E8E9F4,
    ),
    PARCHMENT(
        startArgb = 0xFFF7EDDE,
        endArgb = 0xFFE9D5BD,
        textArgb = 0xFF3A2F22,
        subtitleArgb = 0xA63A2F22,
    ),
    FOREST(
        startArgb = 0xFF205141,
        endArgb = 0xFF122B21,
        textArgb = 0xFFE6F1EA,
        subtitleArgb = 0xA6E6F1EA,
    ),
    WINE(
        startArgb = 0xFF7D3648,
        endArgb = 0xFF3D1A2A,
        textArgb = 0xFFF6E7EC,
        subtitleArgb = 0xA6F6E7EC,
    ),
    SUNSET(
        startArgb = 0xFFE08A5A,
        endArgb = 0xFFA94A5F,
        textArgb = 0xFFFFFFFF,
        subtitleArgb = 0xB8FFFFFF,
    ),
    SKY(
        startArgb = 0xFF7FA8D9,
        endArgb = 0xFF44548C,
        textArgb = 0xFFFFFFFF,
        subtitleArgb = 0xB8FFFFFF,
    ),
    GRAPHITE(
        startArgb = 0xFF2B2B32,
        endArgb = 0xFF131317,
        textArgb = 0xFFECECF2,
        subtitleArgb = 0x99ECECF2,
    ),
}
