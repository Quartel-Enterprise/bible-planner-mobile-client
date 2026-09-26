package com.quare.bibleplanner.core.inappupdate.domain.usecase

import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability

fun interface CheckForUpdate {
    suspend operator fun invoke(): UpdateAvailability
}
