package com.quare.bibleplanner.feature.chat.domain.usecase

data class ChatUseCases(
    val observeConversations: ObserveChatConversationsUseCase,
    val observeMessages: ObserveChatMessagesUseCase,
    val observeQuota: ObserveChatQuotaUseCase,
    val syncRemoteChanges: SyncChatRemoteChangesUseCase,
    val refreshConversations: RefreshChatConversationsUseCase,
    val loadMessages: LoadChatMessagesUseCase,
    val refreshQuota: RefreshChatQuotaUseCase,
    val renameConversation: RenameChatConversationUseCase,
    val deleteConversation: DeleteChatConversationUseCase,
    val getSuggestions: GetChatSuggestions,
    val rememberStudyQuestions: RememberChatStudyQuestionsUseCase,
    val hasStudyQuestions: HasChatStudyQuestionsUseCase,
    val getContext: GetChatContext,
)
