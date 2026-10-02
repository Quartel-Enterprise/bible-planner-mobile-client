package com.quare.bibleplanner.core.daystudy.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyLocalDataSource
import com.quare.bibleplanner.core.daystudy.data.datasource.DayStudyRemoteDataSource
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyResponseDto
import com.quare.bibleplanner.core.daystudy.data.exception.isLimitReachedFailure
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyCacheKeyFactory
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyContentMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyEntityMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyPhaseMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyRequestMapper
import com.quare.bibleplanner.core.daystudy.data.mapper.DayStudyStatusMapper
import com.quare.bibleplanner.core.daystudy.data.model.DayStudyStreamEvent
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyGenerationEventModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyModel
import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyStatusModel
import com.quare.bibleplanner.core.daystudy.domain.repository.DayStudyRepository
import com.quare.bibleplanner.core.model.plan.PassageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal class DayStudyRepositoryImpl(
    private val remoteDataSource: DayStudyRemoteDataSource,
    private val localDataSource: DayStudyLocalDataSource,
    private val requestMapper: DayStudyRequestMapper,
    private val cacheKeyFactory: DayStudyCacheKeyFactory,
    private val contentMapper: DayStudyContentMapper,
    private val entityMapper: DayStudyEntityMapper,
    private val statusMapper: DayStudyStatusMapper,
    private val phaseMapper: DayStudyPhaseMapper,
) : DayStudyRepository {
    override fun getDayStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Flow<DayStudyGenerationEventModel> = flow {
        val request = requestMapper.map(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
        val cacheKey = cacheKeyFactory.create(request).asStorageKey()
        val cached = localDataSource.getByCacheKey(cacheKey)
        if (cached != null) {
            emit(DayStudyGenerationEventModel.Completed(entityMapper.mapToDomain(cached)))
            return@flow
        }
        remoteDataSource.streamDayStudy(request).collect { event ->
            emitStreamEvent(
                event = event,
                cacheKey = cacheKey,
            )
        }
    }.catch { throwable -> throw mapFailure(throwable) }

    private suspend fun FlowCollector<DayStudyGenerationEventModel>.emitStreamEvent(
        event: DayStudyStreamEvent,
        cacheKey: String,
    ) {
        when (event) {
            is DayStudyStreamEvent.Progress -> phaseMapper.map(event.phase)?.let { phase ->
                emit(DayStudyGenerationEventModel.PhaseChanged(phase))
            }

            is DayStudyStreamEvent.Complete -> emit(
                DayStudyGenerationEventModel.Completed(
                    onRemoteSuccess(
                        cacheKey = cacheKey,
                        response = event.response,
                    ),
                ),
            )
        }
    }

    override suspend fun getDayStudyStatus(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): DayStudyStatusModel? {
        val request = requestMapper.map(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
        val status = remoteDataSource
            .fetchStatus(request)
            .map(statusMapper::map)
            .getOrNull() ?: return null
        invalidateStaleLocalCache(
            cacheKey = cacheKeyFactory.create(request).asStorageKey(),
            currentToken = status.cacheToken,
        )
        return status
    }

    // Drop the local copy when the server's cache token no longer matches, so the next
    // getDayStudy re-fetches fresh content (free — the unlock ledger is untouched).
    private suspend fun invalidateStaleLocalCache(
        cacheKey: String,
        currentToken: String,
    ) {
        val cached = localDataSource.getByCacheKey(cacheKey) ?: return
        if (cached.study.cacheToken != currentToken) {
            localDataSource.deleteByCacheKey(cacheKey)
        }
    }

    override suspend fun hasCachedStudy(
        passages: List<PassageModel>,
        version: String,
        languageCode: String,
    ): Boolean {
        val request = requestMapper.map(
            passages = passages,
            version = version,
            languageCode = languageCode,
        )
        return localDataSource.getByCacheKey(cacheKeyFactory.create(request).asStorageKey()) != null
    }

    private suspend fun onRemoteSuccess(
        cacheKey: String,
        response: DayStudyResponseDto,
    ): DayStudyModel {
        localDataSource.save(
            entityMapper.mapToEntities(
                cacheKey = cacheKey,
                response = response,
            ),
        )
        return contentMapper.map(response.content)
    }

    private fun mapFailure(throwable: Throwable): Throwable {
        if (throwable.isLimitReachedFailure()) {
            return LimitReachedException()
        }
        Logger.e(throwable) { "Failed to fetch day study" }
        return throwable
    }
}
