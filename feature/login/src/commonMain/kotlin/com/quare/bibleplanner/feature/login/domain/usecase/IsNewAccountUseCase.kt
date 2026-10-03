package com.quare.bibleplanner.feature.login.domain.usecase

import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.seconds

internal class IsNewAccountUseCase(
    private val observeCurrentUser: ObserveCurrentUser,
) : IsNewAccount {
    private val newAccountWindow = 10.seconds

    override suspend fun invoke(): Boolean {
        val user = observeCurrentUser().first() ?: return false
        val createdAt = user.createdAt ?: return false
        val lastSignInAt = user.lastSignInAt ?: return false
        return lastSignInAt - createdAt < newAccountWindow
    }
}
