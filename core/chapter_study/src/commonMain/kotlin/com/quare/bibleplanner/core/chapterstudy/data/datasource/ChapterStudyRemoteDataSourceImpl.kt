package com.quare.bibleplanner.core.chapterstudy.data.datasource

import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyProgressDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyRequestDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyResponseDto
import com.quare.bibleplanner.core.chapterstudy.data.dto.ChapterStudyStatusDto
import com.quare.bibleplanner.core.chapterstudy.data.model.ChapterStudyStreamEvent
import com.quare.bibleplanner.core.daystudy.data.datasource.StudyFunctionClient
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.functions.FunctionServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json

internal class ChapterStudyRemoteDataSourceImpl(
    private val client: StudyFunctionClient,
    private val json: Json,
) : ChapterStudyRemoteDataSource {
    override fun streamChapterStudy(request: ChapterStudyRequestDto): Flow<ChapterStudyStreamEvent> = client
        .stream(
            functionName = FUNCTION_NAME,
            body = encode(request),
        ).mapNotNull(::mapEventOrNull)

    override suspend fun fetchStatus(request: ChapterStudyRequestDto): Result<ChapterStudyStatusDto> =
        suspendRunCatching {
            val response = client.fetch(
                functionName = STATUS_FUNCTION_NAME,
                body = encode(request),
            )
            json.decodeFromString(ChapterStudyStatusDto.serializer(), response)
        }

    private fun encode(request: ChapterStudyRequestDto): String =
        json.encodeToString(ChapterStudyRequestDto.serializer(), request)

    private fun mapEventOrNull(event: FunctionServerSentEvent): ChapterStudyStreamEvent? {
        val data = event.data ?: return null
        return when (event.event) {
            PROGRESS_EVENT -> ChapterStudyStreamEvent.Progress(
                phase = json.decodeFromString(ChapterStudyProgressDto.serializer(), data).phase,
            )

            COMPLETE_EVENT -> ChapterStudyStreamEvent.Complete(
                response = json.decodeFromString(ChapterStudyResponseDto.serializer(), data),
            )

            ERROR_EVENT -> throw IllegalStateException(data)

            else -> null
        }
    }

    private companion object {
        const val FUNCTION_NAME = "get-chapter-study"
        const val STATUS_FUNCTION_NAME = "get-chapter-study-status"
        const val PROGRESS_EVENT = "progress"
        const val COMPLETE_EVENT = "complete"
        const val ERROR_EVENT = "error"
    }
}
