package com.quare.bibleplanner.feature.chat.presentation

fun interface GetDefaultChatSuggestions {
    suspend operator fun invoke(hasReadingContext: Boolean): List<String>
}
