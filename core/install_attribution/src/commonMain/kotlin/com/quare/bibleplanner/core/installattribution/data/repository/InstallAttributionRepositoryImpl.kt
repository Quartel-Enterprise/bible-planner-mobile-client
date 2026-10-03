package com.quare.bibleplanner.core.installattribution.data.repository

import com.quare.bibleplanner.core.installattribution.data.datasource.InstallAttributionLocalDataSource
import com.quare.bibleplanner.core.installattribution.data.datasource.InstallAttributionRemoteDataSource
import com.quare.bibleplanner.core.installattribution.data.dto.TrackAppInstallRequestDto
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReport
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome
import com.quare.bibleplanner.core.installattribution.domain.repository.InstallAttributionRepository

internal class InstallAttributionRepositoryImpl(
    private val localDataSource: InstallAttributionLocalDataSource,
    private val remoteDataSource: InstallAttributionRemoteDataSource,
) : InstallAttributionRepository {
    override suspend fun isInstallReported(): Boolean = localDataSource.isReported()

    override suspend fun markInstallReported() {
        localDataSource.markReported()
    }

    override suspend fun getOrCreateInstallId(): String = localDataSource.getOrCreateInstallId()

    override suspend fun sendInstallReport(report: InstallReport): InstallReportOutcome =
        remoteDataSource.trackAppInstall(
            TrackAppInstallRequestDto(
                installId = report.installId,
                platform = report.platform,
                oppref = report.oppref,
                gaid = report.advertisingId,
            ),
        )
}
