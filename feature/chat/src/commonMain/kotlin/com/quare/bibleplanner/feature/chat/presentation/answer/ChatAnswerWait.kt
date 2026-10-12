package com.quare.bibleplanner.feature.chat.presentation.answer

import com.quare.bibleplanner.feature.chat.domain.model.ChatMessageModel
import com.quare.bibleplanner.feature.chat.domain.model.ChatRoleModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class ChatAnswerWait {
    private val answerWaitWindow: Duration = 2.minutes
    private val answerWaitRecheck: Duration = 5.seconds
    private var awaitingAnswerJob: Job? = null

    fun isAwaitingAnswer(messages: List<ChatMessageModel>): Boolean {
        val last = messages.lastOrNull() ?: return false
        if (last.role != ChatRoleModel.USER) return false
        return Clock.System.now() - last.createdAt < answerWaitWindow
    }

    fun watch(
        scope: CoroutineScope,
        isAwaitingAnswer: Boolean,
        getMessages: () -> List<ChatMessageModel>,
        onWaitOver: () -> Unit,
    ) {
        if (!isAwaitingAnswer) {
            awaitingAnswerJob?.cancel()
            awaitingAnswerJob = null
            return
        }
        if (awaitingAnswerJob?.isActive == true) return
        awaitingAnswerJob = scope.launch {
            while (isAwaitingAnswer(getMessages())) {
                delay(answerWaitRecheck)
            }
            onWaitOver()
        }
    }
}
