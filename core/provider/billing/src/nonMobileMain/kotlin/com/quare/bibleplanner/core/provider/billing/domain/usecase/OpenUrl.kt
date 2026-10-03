package com.quare.bibleplanner.core.provider.billing.domain.usecase

internal fun interface OpenUrl {
    operator fun invoke(url: String)
}
