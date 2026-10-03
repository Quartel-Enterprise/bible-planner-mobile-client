package com.quare.bibleplanner.core.studyunlock.domain.usecase

fun interface GatherAdsConsentIfEnabled {
    suspend operator fun invoke()
}
