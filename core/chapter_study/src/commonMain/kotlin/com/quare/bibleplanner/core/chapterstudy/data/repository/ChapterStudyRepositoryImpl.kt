package com.quare.bibleplanner.core.chapterstudy.data.repository

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyLocalDataSource
import com.quare.bibleplanner.core.chapterstudy.data.datasource.ChapterStudyRemoteDataSource
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyResponseDto
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyCacheKeyFactory
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyEntityMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyPhaseMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyRequestMapper
import com.quare.bibleplanner.core.chapterstudy.data.mapper.ChapterStudyStatusMapper
import com.quare.bibleplanner.core.chapterstudy.data.model.ChapterStudyStreamEvent
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyGenerationEventModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyModel
import com.quare.bibleplanner.core.chapterstudy.domain.model.ChapterStudyStatusModel
import com.quare.bibleplanner.core.chapterstudy.domain.repository.ChapterStudyRepository
import com.quare.bibleplanner.core.daystudy.data.exception.isLimitReachedFailure
import com.quare.bibleplanner.core.daystudy.domain.exception.LimitReachedException
import com.quare.bibleplanner.core.model.book.ChapterRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal class ChapterStudyRepositoryImpl(
    private val remoteDataSource: ChapterStudyRemoteDataSource,
    private val localDataSource: ChapterStudyLocalDataSource,
    private val requestMapper: ChapterStudyRequestMapper,
    private val cacheKeyFactory: ChapterStudyCacheKeyFactory,
    private val entityMapper: ChapterStudyEntityMapper,
    private val statusMapper: ChapterStudyStatusMapper,
    private val phaseMapper: ChapterStudyPhaseMapper,
) : ChapterStudyRepository {
    override fun generateChapterStudy(
        chapter: ChapterRef,
        languageCode: String,
    ): Flow<ChapterStudyGenerationEventModel> = flow {
        val cacheKey = cacheKeyFactory.create(
            chapter = chapter,
            languageCode = languageCode,
        )
        val request = requestMapper.map(
            chapter = chapter,
            languageCode = languageCode,
        )
        remoteDataSource.streamChapterStudy(request).collect { event ->
            emitStreamEvent(
                event = event,
                cacheKey = cacheKey,
            )
        }
    }.catch { throwable -> throw mapFailure(throwable) }

    override suspend fun fetchStatus(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyStatusModel? {
        val request = requestMapper.map(
            chapter = chapter,
            languageCode = languageCode,
        )
        val status = remoteDataSource
            .fetchStatus(request)
            .map(statusMapper::map)
            .getOrNull() ?: return null
        invalidateStaleLocalCache(
            cacheKey = cacheKeyFactory.create(
                chapter = chapter,
                languageCode = languageCode,
            ),
            currentToken = status.cacheToken,
        )
        return status
    }

    override suspend fun findCachedStudy(
        chapter: ChapterRef,
        languageCode: String,
    ): ChapterStudyModel? = localDataSource
        .findByCacheKey(
            cacheKeyFactory.create(
                chapter = chapter,
                languageCode = languageCode,
            ),
        )?.let(entityMapper::mapToDomain)

    override suspend fun clearCache() {
        localDataSource.deleteAll()
    }

    private suspend fun FlowCollector<ChapterStudyGenerationEventModel>.emitStreamEvent(
        event: ChapterStudyStreamEvent,
        cacheKey: String,
    ) {
        when (event) {
            is ChapterStudyStreamEvent.Progress -> phaseMapper.mapOrNull(event.phase)?.let { phase ->
                emit(ChapterStudyGenerationEventModel.PhaseChanged(phase))
            }

            is ChapterStudyStreamEvent.Complete -> emit(
                ChapterStudyGenerationEventModel.Completed(
                    saveStudy(
                        cacheKey = cacheKey,
                        response = event.response,
                    ),
                ),
            )
        }
    }

    private suspend fun saveStudy(
        cacheKey: String,
        response: ChapterStudyResponseDto,
    ): ChapterStudyModel {
        val content = entityMapper.mapToEntities(
            cacheKey = cacheKey,
            response = response,
        )
        localDataSource.save(content)
        return entityMapper.mapToDomain(content)
    }

    private suspend fun invalidateStaleLocalCache(
        cacheKey: String,
        currentToken: String,
    ) {
        val cached = localDataSource.findByCacheKey(cacheKey) ?: return
        if (cached.study.cacheToken != currentToken) {
            localDataSource.deleteByCacheKey(cacheKey)
        }
    }

    private fun mapFailure(throwable: Throwable): Throwable {
        if (throwable.isLimitReachedFailure()) {
            return LimitReachedException()
        }
        Logger.e(throwable) { "Failed to fetch chapter study" }
        return throwable
    }
}
