package com.quare.bibleplanner.core.sync.domain.usecase

fun interface ObserveSync {
    suspend operator fun invoke()
}
