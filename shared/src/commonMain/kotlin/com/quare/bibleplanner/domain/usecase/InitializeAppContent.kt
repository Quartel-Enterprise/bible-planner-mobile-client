package com.quare.bibleplanner.domain.usecase

import kotlinx.coroutines.CoroutineScope

fun interface InitializeAppContent {
    operator fun invoke(coroutineScope: CoroutineScope)
}
