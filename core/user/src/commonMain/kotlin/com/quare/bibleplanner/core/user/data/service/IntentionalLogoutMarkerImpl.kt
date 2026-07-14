package com.quare.bibleplanner.core.user.data.service

import com.quare.bibleplanner.core.user.domain.service.IntentionalLogoutMarker
import kotlinx.coroutines.flow.MutableStateFlow

internal class IntentionalLogoutMarkerImpl : IntentionalLogoutMarker {
    private val isMarked = MutableStateFlow(false)

    override fun mark() {
        isMarked.value = true
    }

    override fun unmark() {
        isMarked.value = false
    }

    override fun consume(): Boolean = isMarked.compareAndSet(
        expect = true,
        update = false,
    )
}
