package com.quare.bibleplanner.core.inappupdate.domain.model

sealed interface UpdateAvailability {
    sealed interface Pending : UpdateAvailability

    data class Available(
        val versionName: String?,
    ) : Pending

    data object Downloaded : Pending

    data object NotAvailable : UpdateAvailability

    data object CheckFailed : UpdateAvailability
}
