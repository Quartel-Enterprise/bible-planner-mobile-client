package com.quare.bibleplanner.feature.inappupdate.domain.usecase

import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability

fun interface CheckForUpdate {
    suspend operator fun invoke(): UpdateAvailability
}
