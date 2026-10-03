package com.quare.bibleplanner.core.model.route

import kotlinx.serialization.Serializable

@Serializable
data class StudyUnlockNavRoute(
    val surface: StudyUnlockSurface,
    val paywallSource: PaywallEntrySource,
    val requestKey: String,
    val rewardedRemainingToday: Int,
) : NavRoute
