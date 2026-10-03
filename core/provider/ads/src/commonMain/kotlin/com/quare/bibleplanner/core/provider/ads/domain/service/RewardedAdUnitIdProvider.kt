package com.quare.bibleplanner.core.provider.ads.domain.service

fun interface RewardedAdUnitIdProvider {
    operator fun invoke(): String
}
