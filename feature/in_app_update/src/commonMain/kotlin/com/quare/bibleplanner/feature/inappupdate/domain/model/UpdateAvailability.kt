package com.quare.bibleplanner.feature.inappupdate.domain.model

sealed interface UpdateAvailability {
    data class Available(
        val versionName: String?,
    ) : UpdateAvailability

    data object NotAvailable : UpdateAvailability
}
