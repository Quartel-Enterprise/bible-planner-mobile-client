package com.quare.bibleplanner.core.installattribution.domain.model

enum class InstallReportOutcome {
    DELIVERED,

    // Why: a 4xx means sending the same report again would be refused too, so it is not retried.
    REJECTED,

    FAILED,
}
