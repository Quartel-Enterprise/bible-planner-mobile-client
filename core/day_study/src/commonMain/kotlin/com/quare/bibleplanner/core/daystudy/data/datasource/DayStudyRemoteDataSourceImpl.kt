package com.quare.bibleplanner.core.daystudy.data.datasource

import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyProgressDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyRequestDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyResponseDto
import com.quare.bibleplanner.core.daystudy.data.dto.DayStudyStatusDto
import com.quare.bibleplanner.core.daystudy.data.model.DayStudyStreamEvent
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.functions.FunctionServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json

internal class DayStudyRemoteDataSourceImpl(
    private val client: StudyFunctionClient,
    private val json: Json,
) : DayStudyRemoteDataSource {
    private val requestJson = Json { explicitNulls = false }

    override fun streamDayStudy(request: DayStudyRequestDto): Flow<DayStudyStreamEvent> = client
        .stream(
            functionName = FUNCTION_NAME,
            body = encode(request),
        ).mapNotNull(::mapEvent)

    override suspend fun fetchStatus(request: DayStudyRequestDto): Result<DayStudyStatusDto> = suspendRunCatching {
        val response = client.fetch(
            functionName = STATUS_FUNCTION_NAME,
            body = encode(request),
        )
        json.decodeFromString(DayStudyStatusDto.serializer(), response)
    }

    private fun encode(request: DayStudyRequestDto): String =
        requestJson.encodeToString(DayStudyRequestDto.serializer(), request)

    private fun mapEvent(event: FunctionServerSentEvent): DayStudyStreamEvent? {
        val data = event.data ?: return null
        return when (event.event) {
            PROGRESS_EVENT -> DayStudyStreamEvent.Progress(
                phase = json.decodeFromString(DayStudyProgressDto.serializer(), data).phase,
            )

            COMPLETE_EVENT -> DayStudyStreamEvent.Complete(
                response = json.decodeFromString(DayStudyResponseDto.serializer(), data),
            )

            ERROR_EVENT -> throw IllegalStateException(data)

            else -> null
        }
    }

    private companion object {
        const val FUNCTION_NAME = "get-day-study"
        const val STATUS_FUNCTION_NAME = "get-day-study-status"
        const val PROGRESS_EVENT = "progress"
        const val COMPLETE_EVENT = "complete"
        const val ERROR_EVENT = "error"
    }
}
