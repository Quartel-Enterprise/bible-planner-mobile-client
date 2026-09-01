package com.quare.bibleplanner.feature.chat.presentation

import bibleplanner.feature.chat.generated.resources.Res
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_application
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_context
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_grace
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_plan
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_start
import bibleplanner.feature.chat.generated.resources.chat_default_suggestion_summary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

private val readingSuggestions: List<StringResource> = listOf(
    Res.string.chat_default_suggestion_summary,
    Res.string.chat_default_suggestion_context,
    Res.string.chat_default_suggestion_application,
)
private val openSuggestions: List<StringResource> = listOf(
    Res.string.chat_default_suggestion_start,
    Res.string.chat_default_suggestion_plan,
    Res.string.chat_default_suggestion_grace,
)

internal class GetDefaultChatSuggestionsUseCase : GetDefaultChatSuggestions {
    override suspend fun invoke(hasReadingContext: Boolean): List<String> =
        (if (hasReadingContext) readingSuggestions else openSuggestions).map { resource -> getString(resource) }
}
