package com.quare.bibleplanner.feature.read.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.quare.bibleplanner.core.books.domain.usecase.GetChapterIdUseCase
import com.quare.bibleplanner.core.books.domain.usecase.GetSelectedVersionIdFlowUseCase
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.provider.room.dao.VerseDao
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiAction
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiEvent
import com.quare.bibleplanner.feature.read.presentation.model.ReadUiState
import com.quare.bibleplanner.feature.read.presentation.model.VerseUiModel
import com.quare.bibleplanner.ui.utils.observe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReadViewModel(
    savedStateHandle: SavedStateHandle,
    private val verseDao: VerseDao,
    private val getSelectedVersionIdFlow: GetSelectedVersionIdFlowUseCase,
    private val getChapterId: GetChapterIdUseCase,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<ReadNavRoute>()
    private val bookId = BookId.valueOf(route.bookId)
    private val bookStringResource = bookId.toBookNameResource()
    private val loadingState = route.toLoadingState()
    private val errorState = route.toErrorState()

    private val _uiState = MutableStateFlow<ReadUiState>(loadingState)
    val uiState: StateFlow<ReadUiState> = _uiState

    private val _uiAction = MutableSharedFlow<ReadUiAction>()
    val uiAction: SharedFlow<ReadUiAction> = _uiAction

    init {
        loadChapterContent()
    }

    fun onEvent(event: ReadUiEvent) {
        when (event) {
            ReadUiEvent.OnArrowBackClick -> {
                navigateBack()
            }

            ReadUiEvent.OnRetryClick -> {
                loadChapterContent()
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _uiAction.emit(ReadUiAction.NavigateBack)
        }
    }

    private fun loadChapterContent() {
        _uiState.update { loadingState }
        observe(getSelectedVersionIdFlow()) { versionId ->
            val versesWithTexts = verseDao.getVersesWithTextsByChapterId(
                chapterId = getChapterId(bookId, chapterNumber = route.chapterNumber) ?: run {
                    updateToErrorState()
                    return@observe
                },
            )
            if (versesWithTexts.isEmpty()) {
                updateToErrorState()
                return@observe
            }

            val verseUiModels = versesWithTexts.map { verseWithTexts ->
                verseWithTexts.texts.find { it.bibleVersionId == versionId }?.text?.let { safeText ->
                    VerseUiModel(
                        number = verseWithTexts.verse.number,
                        text = safeText,
                    )
                } ?: run {
                    updateToErrorState()
                    return@observe
                }
            }

            _uiState.update {
                ReadUiState.Success(
                    bookStringResource = bookStringResource,
                    chapterNumber = route.chapterNumber,
                    verses = verseUiModels,
                )
            }
        }
    }

    private fun updateToErrorState() {
        _uiState.update { errorState }
    }

    private fun ReadNavRoute.toLoadingState(): ReadUiState.Loading = ReadUiState.Loading(
        chapterNumber = chapterNumber,
        bookStringResource = bookStringResource,
    )

    private fun ReadNavRoute.toErrorState(): ReadUiState.Error = ReadUiState.Error(
        chapterNumber = chapterNumber,
        bookStringResource = bookStringResource,
    )
}
