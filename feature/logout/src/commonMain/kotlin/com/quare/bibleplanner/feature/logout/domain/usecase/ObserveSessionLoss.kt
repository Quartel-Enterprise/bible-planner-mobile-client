package com.quare.bibleplanner.feature.logout.domain.usecase

fun interface ObserveSessionLoss {
    suspend operator fun invoke()
}
