package com.quare.bibleplanner.feature.login.domain.usecase

import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.seconds

/*
 * Why: native Google/Apple sign-in always reports SessionSource.SignIn, so a new account
 * is detected from created_at vs last_sign_in_at (same transaction on sign-up). Missing
 * timestamps return false so unprovable sign-ups don't inflate acquisition.
 */
internal class IsNewAccountUseCase(
    private val observeCurrentUser: ObserveCurrentUser,
) : IsNewAccount {
    /*
     * Why: wide enough to absorb the lag between Supabase's two writes, far shorter than any
     * real gap between two separate sign-ins.
     */
    private val newAccountWindow = 10.seconds

    override suspend fun invoke(): Boolean {
        val user = observeCurrentUser().first() ?: return false
        val createdAt = user.createdAt ?: return false
        val lastSignInAt = user.lastSignInAt ?: return false
        return lastSignInAt - createdAt < newAccountWindow
    }
}
