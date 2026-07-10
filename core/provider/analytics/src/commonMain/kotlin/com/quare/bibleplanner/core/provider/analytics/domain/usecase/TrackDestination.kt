package com.quare.bibleplanner.core.provider.analytics.domain.usecase

import androidx.navigation3.runtime.NavKey

fun interface TrackDestination {
    operator fun invoke(navKey: NavKey)
}
