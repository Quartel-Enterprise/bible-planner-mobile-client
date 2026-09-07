package com.quare.bibleplanner.feature.login.domain.usecase

import com.quare.bibleplanner.core.user.domain.usecase.ObserveCurrentUser
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.seconds

/**
 * Reads the answer off the user record: Supabase stamps `created_at` and `last_sign_in_at` in the
 * same transaction when it creates the account, so on the very first sign-in the two are a moment
 * apart, while every later sign-in leaves at least a whole session between them.
 *
 * The session source cannot answer this instead. `SessionSource.SignUp` only ever describes
 * Supabase's email/password `signUpWith` flow, and the native Google/Apple sign-in this app uses
 * always reports `SessionSource.SignIn` — whether or not the account already existed.
 *
 * Returns `false` when either timestamp is missing, so an unprovable sign-up is reported as an
 * ordinary [login][com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsEventNames.LOGIN]
 * rather than inflating acquisition.
 */
internal class IsNewAccountUseCase(
    private val observeCurrentUser: ObserveCurrentUser,
) : IsNewAccount {
    override suspend fun invoke(): Boolean {
        val user = observeCurrentUser().first() ?: return false
        val createdAt = user.createdAt ?: return false
        val lastSignInAt = user.lastSignInAt ?: return false
        return lastSignInAt - createdAt < NEW_ACCOUNT_WINDOW
    }

    private companion object {
        /**
         * How far `last_sign_in_at` may sit past `created_at` and still count as the account's first
         * sign-in: wide enough to absorb the lag between the two writes, far shorter than any real
         * gap between two separate sign-ins.
         */
        val NEW_ACCOUNT_WINDOW = 10.seconds
    }
}
