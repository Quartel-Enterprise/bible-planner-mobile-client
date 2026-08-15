package com.quare.bibleplanner.feature.chat.presentation.model

import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames
import com.quare.bibleplanner.core.provider.analytics.domain.model.EventAnalytics
import com.quare.bibleplanner.ui.utils.presentation.UiEvent

sealed interface ChatUiEvent : UiEvent {
    data class OnInputChanged(
        val text: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnSendClick : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(AnalyticsEventNames.AI_CHAT_MESSAGE_SENT)
    }

    data class OnSuggestionClick(
        val suggestion: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.AI_CHAT_SUGGESTION_CLICKED,
                AnalyticsEventNames.AI_CHAT_MESSAGE_SENT,
            ),
        )
    }

    data object OnSuggestionBarToggle : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnRetryClick : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.AI_CHAT_RETRY_CLICKED,
            params = emptyMap(),
        )
    }

    data object OnSubscribeClick : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(
            setOf(
                AnalyticsEventNames.AI_CHAT_SUBSCRIBE_CLICKED,
                AnalyticsEventNames.PAYWALL_VIEWED,
            ),
        )
    }

    data object OnHistoryClick : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Manual(AnalyticsEventNames.AI_CHAT_HISTORY_OPENED)
    }

    data object OnHistoryDismiss : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnHistoryQueryChanged(
        val query: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnNewConversationClick : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.AI_CHAT_NEW_CONVERSATION_CLICKED,
            params = emptyMap(),
        )
    }

    data class OnConversationClick(
        val conversationId: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.AI_CHAT_CONVERSATION_SELECTED,
            params = emptyMap(),
        )
    }

    data class OnConversationActionsToggle(
        val conversationId: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnRenameConversationClick(
        val conversationId: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnRenameDraftChanged(
        val title: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnRenameConfirm : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.AI_CHAT_CONVERSATION_RENAMED,
            params = emptyMap(),
        )
    }

    data object OnRenameCancel : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data class OnDeleteConversationClick(
        val conversationId: String,
    ) : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }

    data object OnDeleteConfirm : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.Track.Automatic(
            name = AnalyticsEventNames.AI_CHAT_CONVERSATION_DELETED,
            params = emptyMap(),
        )
    }

    data object OnDeleteCancel : ChatUiEvent {
        override val analytics: EventAnalytics = EventAnalytics.NotTracked
    }
}
