package com.quare.bibleplanner.feature.chat.presentation.model

enum class ChatInputMode {
    /** Normal composing state. */
    ENABLED,

    /** Rate limited: sending is blocked until the countdown ends. */
    COOLDOWN,

    /** Free questions are over; the bar becomes the paywall entry. */
    LOCKED,
}
