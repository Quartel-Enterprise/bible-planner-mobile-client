package com.quare.bibleplanner.core.user.domain.service

interface IntentionalLogoutMarker {
    fun mark()

    fun unmark()

    fun consume(): Boolean
}
