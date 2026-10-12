package com.quare.bibleplanner.core.studyunlock.domain.store

import com.quare.bibleplanner.core.studyunlock.domain.model.StudyUnlockResultModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update

class StudyUnlockResultStore {
    private val resultsByRequestKey = MutableStateFlow<Map<String, StudyUnlockResultModel>>(emptyMap())

    fun publishEarned(requestKey: String) {
        publish(
            requestKey = requestKey,
            result = StudyUnlockResultModel.REWARD_EARNED,
        )
    }

    fun publishUnlockedWithoutVideo(requestKey: String) {
        publish(
            requestKey = requestKey,
            result = StudyUnlockResultModel.UNLOCKED_WITHOUT_VIDEO,
        )
    }

    fun observeEarned(requestKey: String): Flow<Unit> = observeResult(requestKey).map { }

    fun observeResult(requestKey: String): Flow<StudyUnlockResultModel> = resultsByRequestKey
        .mapNotNull { results -> results[requestKey] }
        .map { result ->
            resultsByRequestKey.update { results -> results - requestKey }
            result
        }

    private fun publish(
        requestKey: String,
        result: StudyUnlockResultModel,
    ) {
        resultsByRequestKey.update { results -> results + (requestKey to result) }
    }
}
