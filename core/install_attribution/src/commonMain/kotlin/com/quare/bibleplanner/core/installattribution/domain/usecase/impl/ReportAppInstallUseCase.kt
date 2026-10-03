package com.quare.bibleplanner.core.installattribution.domain.usecase.impl

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.installattribution.data.mapper.InstallReferrerParser
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReport
import com.quare.bibleplanner.core.installattribution.domain.model.InstallReportOutcome
import com.quare.bibleplanner.core.installattribution.domain.repository.InstallAttributionRepository
import com.quare.bibleplanner.core.installattribution.domain.usecase.ReadInstallSource
import com.quare.bibleplanner.core.installattribution.domain.usecase.ReportAppInstall
import com.quare.bibleplanner.core.utils.suspendRunCatching

internal class ReportAppInstallUseCase(
    private val repository: InstallAttributionRepository,
    private val readInstallSource: ReadInstallSource,
    private val referrerParser: InstallReferrerParser,
) : ReportAppInstall {
    override suspend fun invoke() {
        suspendRunCatching { reportIfNeeded() }
            .onFailure { throwable ->
                Logger.w(tag = TAG, throwable = throwable, messageString = "Failed to report the app install")
            }
    }

    private suspend fun reportIfNeeded() {
        if (repository.isInstallReported()) return
        val source = readInstallSource() ?: return
        // Why: persisted before sending, so a retry on a later start reuses it and the backend dedupes by it.
        val installId = repository.getOrCreateInstallId()
        val report = InstallReport(
            installId = installId,
            platform = source.platform,
            oppref = referrerParser.parseOppref(source.referrer),
            advertisingId = source.advertisingId,
        )
        val outcome = repository.sendInstallReport(report)
        if (outcome == InstallReportOutcome.FAILED) {
            Logger.w(tag = TAG) { "App install report failed, retrying on a later start" }
        } else {
            repository.markInstallReported()
        }
    }

    private companion object {
        const val TAG = "ReportAppInstall"
    }
}
