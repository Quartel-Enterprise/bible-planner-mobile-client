package com.quare.bibleplanner.feature.login.domain.usecase

/**
 * Tells whether the session that was just established created the account rather than signing an
 * existing one back in, so the auth success can be reported as `sign_up` instead of `login`.
 */
fun interface IsNewAccount {
    suspend operator fun invoke(): Boolean
}
