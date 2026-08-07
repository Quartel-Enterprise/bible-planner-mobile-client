package com.quare.bibleplanner.feature.chat.presentation

/**
 * The starter questions shown when the reading has no generated study to borrow its common
 * questions from — which is most of the time, since the chat is usually opened straight from the
 * day. They are passage-agnostic on purpose: they read well against any chapter.
 */
fun interface GetDefaultChatSuggestions {
    suspend operator fun invoke(hasReadingContext: Boolean): List<String>
}
