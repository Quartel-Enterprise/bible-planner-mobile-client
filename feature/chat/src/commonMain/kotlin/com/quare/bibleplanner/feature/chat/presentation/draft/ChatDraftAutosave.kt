package com.quare.bibleplanner.feature.chat.presentation.draft

import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.feature.chat.domain.model.PendingDraftModel
import com.quare.bibleplanner.feature.chat.domain.usecase.SaveChatDraftUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    fun schedule(
        scope: CoroutineScope,
        draft: PendingDraftModel,
    ) {
        pendingDraft = draft
        draftSaveJob?.cancel()
        draftSaveJob = scope.launch {
            delay(draftDebounceDelay)
            saveDraft(draft)
            pendingDraft = null
        }
    }

    fun clear(
        scope: CoroutineScope,
        threadKey: String,
    ) {
        pendingDraft = null
        draftSaveJob?.cancel()
        val cleared = PendingDraftModel(
            threadKey = threadKey,
            content = "",
        )
        scope.launch { saveDraft(cleared) }
    }

    // Why: the screen's scope is gone by now, so a draft still waiting for its debounce is saved on the app's.
    fun flush() {
        val unsaved = pendingDraft ?: return
        if (draftSaveJob?.isActive != true) return
        draftSaveJob?.cancel()
        applicationScope.launch { saveDraft(unsaved) }
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
