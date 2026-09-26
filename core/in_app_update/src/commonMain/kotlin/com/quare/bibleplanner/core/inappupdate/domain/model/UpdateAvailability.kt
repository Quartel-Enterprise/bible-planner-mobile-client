package com.quare.bibleplanner.core.inappupdate.domain.model

sealed interface UpdateAvailability {
    data class Available(
        val versionName: String?,
    ) : UpdateAvailability

    data object NotAvailable : UpdateAvailability
}
