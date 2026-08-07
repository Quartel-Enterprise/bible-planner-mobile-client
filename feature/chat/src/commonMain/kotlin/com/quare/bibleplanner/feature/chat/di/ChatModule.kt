package com.quare.bibleplanner.feature.chat.di

import com.quare.bibleplanner.core.clear.domain.ClearChatLocalData
import com.quare.bibleplanner.feature.chat.data.datasource.ChatConversationsRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatLocalDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatMessagesRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatRealtimeDataSource
import com.quare.bibleplanner.feature.chat.data.datasource.ChatStreamRemoteDataSource
import com.quare.bibleplanner.feature.chat.data.mapper.ChatContextRequestMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatConversationMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatEntityMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatMessageMapper
import com.quare.bibleplanner.feature.chat.data.mapper.ChatQuotaMapper
import com.quare.bibleplanner.feature.chat.data.repository.ChatRepositoryImpl
import com.quare.bibleplanner.feature.chat.domain.coordinator.ChatStreamCoordinator
import com.quare.bibleplanner.feature.chat.domain.coordinator.ChatStreamCoordinatorImpl
import com.quare.bibleplanner.feature.chat.domain.repository.ChatRepository
import com.quare.bibleplanner.feature.chat.domain.usecase.ChatUseCases
import com.quare.bibleplanner.feature.chat.domain.usecase.DeleteChatConversationUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.GetChatContext
import com.quare.bibleplanner.feature.chat.domain.usecase.GetChatSuggestions
import com.quare.bibleplanner.feature.chat.domain.usecase.LoadChatMessagesUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.ObserveChatConversationsUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.ObserveChatMessagesUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.ObserveChatQuotaUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.RefreshChatConversationsUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.RefreshChatQuotaUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.RenameChatConversationUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.SendChatMessageUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.SyncChatRemoteChangesUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.impl.ClearChatLocalDataUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.impl.GetChatContextUseCase
import com.quare.bibleplanner.feature.chat.domain.usecase.impl.GetChatSuggestionsUseCase
import com.quare.bibleplanner.feature.chat.presentation.GetDefaultChatSuggestions
import com.quare.bibleplanner.feature.chat.presentation.GetDefaultChatSuggestionsUseCase
import com.quare.bibleplanner.feature.chat.presentation.mapper.ChatConversationGroupMapper
import com.quare.bibleplanner.feature.chat.presentation.mapper.ChatMessageUiMapper
import com.quare.bibleplanner.feature.chat.presentation.viewmodel.ChatViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatModule = module {
    factoryOf(::ChatConversationMapper)
    factoryOf(::ChatMessageMapper)
    factoryOf(::ChatQuotaMapper)
    factoryOf(::ChatContextRequestMapper)
    factoryOf(::ChatEntityMapper)
    factoryOf(::ChatMessageUiMapper)
    factoryOf(::ChatConversationGroupMapper)
    factoryOf(::GetDefaultChatSuggestionsUseCase).bind<GetDefaultChatSuggestions>()

    singleOf(::ChatLocalDataSource)
    singleOf(::ChatStreamRemoteDataSource)
    singleOf(::ChatConversationsRemoteDataSource)
    singleOf(::ChatMessagesRemoteDataSource)
    singleOf(::ChatRealtimeDataSource)
    // Single: the session store lives here, so every screen and the coordinator share one state.
    singleOf(::ChatRepositoryImpl).bind<ChatRepository>()
    singleOf(::ChatStreamCoordinatorImpl).bind<ChatStreamCoordinator>()

    factoryOf(::SendChatMessageUseCase)
    factoryOf(::ObserveChatConversationsUseCase)
    factoryOf(::ObserveChatMessagesUseCase)
    factoryOf(::ObserveChatQuotaUseCase)
    factoryOf(::SyncChatRemoteChangesUseCase)
    factoryOf(::RefreshChatConversationsUseCase)
    factoryOf(::LoadChatMessagesUseCase)
    factoryOf(::RefreshChatQuotaUseCase)
    factoryOf(::RenameChatConversationUseCase)
    factoryOf(::DeleteChatConversationUseCase)
    factoryOf(::GetChatSuggestionsUseCase).bind<GetChatSuggestions>()
    factoryOf(::GetChatContextUseCase).bind<GetChatContext>()
    factoryOf(::ClearChatLocalDataUseCase).bind<ClearChatLocalData>()
    factoryOf(::ChatUseCases)

    viewModelOf(::ChatViewModel)
}
