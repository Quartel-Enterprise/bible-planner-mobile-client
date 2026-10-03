package com.quare.bibleplanner.core.daystudy.testing

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeDayStudyRepository(
    var hasCached: Boolean,
    var status: DayStudyStatusModel?,
    var statusError: Throwable?,
    var events: List<DayStudyGenerationEventModel>,
) : DayStudyRepository {
    var eventsError: Throwable? = null
    var neverCompletes = false
    var statusGate: CompletableDeferred<Unit>? = null
    val studyRequests = mutableListOf<DayStudyRequest>()
    val studyRewardFlags = mutableListOf<Boolean>()
    val cacheLookups = mutableListOf<DayStudyRequest>()

    override fun getDayStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
        isRewarded: Boolean,
    ): Flow<DayStudyGenerationEventModel> = flow {
        studyRequests += DayStudyRequest(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
        studyRewardFlags += isRewarded
        events.forEach { event -> emit(event) }
        eventsError?.let { error -> throw error }
        if (neverCompletes) awaitCancellation()
    }

    override suspend fun getDayStudyStatus(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyStatusModel? {
        statusGate?.await()
        statusError?.let { error -> throw error }
        return status
    }

    override suspend fun hasCachedStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Boolean {
        cacheLookups += DayStudyRequest(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
        return hasCached
    }
}
