package com.quare.bibleplanner.core.inappupdate.fake

import com.quare.bibleplanner.core.inappupdate.domain.UpdatePromptPreferences

internal class FakeUpdatePromptPreferences(
    private var lastPromptedAt: Long?,
) : UpdatePromptPreferences {
    override suspend fun getLastPromptedAt(): Long? = lastPromptedAt

    override suspend fun setLastPromptedAt(timestamp: Long) {
        lastPromptedAt = timestamp
    }
}
