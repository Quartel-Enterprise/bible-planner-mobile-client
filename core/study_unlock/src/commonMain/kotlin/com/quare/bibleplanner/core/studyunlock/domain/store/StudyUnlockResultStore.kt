package com.quare.bibleplanner.core.studyunlock.domain.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class StudyUnlockResultStore {
    private val earnedRequestKeys = MutableStateFlow<Set<String>>(emptySet())

    fun publishEarned(requestKey: String) {
        earnedRequestKeys.update { keys -> keys + requestKey }
    }

    fun observeEarned(requestKey: String): Flow<Unit> = earnedRequestKeys
        .filter { keys -> requestKey in keys }
        .map { earnedRequestKeys.update { keys -> keys - requestKey } }
}
