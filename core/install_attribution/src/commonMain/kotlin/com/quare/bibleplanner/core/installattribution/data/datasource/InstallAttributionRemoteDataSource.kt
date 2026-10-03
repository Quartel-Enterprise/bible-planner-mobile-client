package com.quare.bibleplanner.core.installattribution.data.datasource

import com.quare.bibleplanner.core.installattribution.data.dto.TrackAppInstallRequestDto
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.functions.Functions
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

internal class InstallAttributionRemoteDataSource(
    private val functions: Functions,
    private val json: Json,
) {
    private val clientErrorStatuses = 400..499

    suspend fun trackAppInstall(request: TrackAppInstallRequestDto): InstallReportOutcome = suspendRunCatching {
        functions.invoke(FUNCTION_TRACK_APP_INSTALL) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(TrackAppInstallRequestDto.serializer(), request))
        }
        InstallReportOutcome.DELIVERED
    }.getOrElse { throwable ->
        if (throwable is RestException && throwable.statusCode in clientErrorStatuses) {
            InstallReportOutcome.REJECTED
        } else {
            InstallReportOutcome.FAILED
        }
    }

    private companion object {
        const val FUNCTION_TRACK_APP_INSTALL = "track-app-install"
    }
}
