package com.quare.bibleplanner.feature.chat.presentation.draft

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import com.quare.bibleplanner.feature.chat.domain.usecase.SaveChatDraftUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ChatDraftAutosave(
    private val saveDraft: SaveChatDraftUseCase,
    private val applicationScope: ApplicationScope,
) {
    private val draftDebounceDelay: Duration = 2.seconds
    private var draftSaveJob: Job? = null
    private var pendingDraft: PendingDraftModel? = null
    private var lastAppliedDraft: String? = null

    /*
     * Why: debounced saves run on the screen's scope and the clear/flush saves on the app's; the lock
     * keeps a cancelled write still in flight from landing after a newer one for the same thread.
     */
    private val draftWrites: Mutex = Mutex()

    fun schedule(
        scope: CoroutineScope,
        draft: PendingDraftModel,
    ) {
        pendingDraft = draft
        draftSaveJob?.cancel()
        draftSaveJob = scope.launch {
            delay(draftDebounceDelay)
            writeDraft(draft).onSuccess {
                if (pendingDraft === draft) pendingDraft = null
            }
        }
    }

    // Why: on the app's scope so closing the screen right after sending can't cancel the clearing write.
    fun clear(threadKey: String) {
        pendingDraft = null
        draftSaveJob?.cancel()
        val cleared = PendingDraftModel(
            threadKey = threadKey,
            content = "",
        )
        applicationScope.launch { writeDraft(cleared) }
    }

    /*
     * Why: ViewModel.clear() closes the screen's scope before onCleared() calls this, so the debounce job
     * is already cancelled; only pendingDraft still says whether a save is owed.
     */
    fun flush() {
        val unsaved = pendingDraft ?: return
        draftSaveJob?.cancel()
        applicationScope.launch { writeDraft(unsaved) }
    }

    private suspend fun writeDraft(draft: PendingDraftModel): Result<Unit> = draftWrites.withLock {
        suspendRunCatching { saveDraft(draft) }
            .onFailure { throwable -> Logger.e(throwable) { "Failed to save the chat draft" } }
    }

    /*
     * Why: a stored draft only replaces an input the person hasn't typed over, so a late emission of
     * the thread's draft never wipes what they are writing.
     */
    fun findInputToApply(
        draft: String,
        input: String,
    ): String? {
        if (input == draft) {
            lastAppliedDraft = draft
            return null
        }
        if (input.isNotEmpty() && input != lastAppliedDraft) return null
        lastAppliedDraft = draft
        return draft
    }
}
