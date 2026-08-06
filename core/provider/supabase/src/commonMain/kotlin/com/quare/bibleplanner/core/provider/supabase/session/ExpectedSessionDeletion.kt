package com.quare.bibleplanner.core.provider.supabase.session

import kotlinx.coroutines.flow.MutableStateFlow

class ExpectedSessionDeletion {
    private val isDeletionExpected = MutableStateFlow(false)

    val isExpected: Boolean
        get() = isDeletionExpected.value

    suspend fun whileDeleting(block: suspend () -> Unit) {
        isDeletionExpected.value = true
        try {
            block()
        } finally {
            isDeletionExpected.value = false
        }
    }
}
