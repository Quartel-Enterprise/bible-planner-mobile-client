package com.quare.bibleplanner.core.installattribution.domain.repository

import com.quare.bibleplanner.core.installattribution.domain.model.InstallReport
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome

interface InstallAttributionRepository {
    suspend fun isInstallReported(): Boolean

    suspend fun markInstallReported()

    suspend fun getOrCreateInstallId(): String

    suspend fun sendInstallReport(report: InstallReport): InstallReportOutcome
}
