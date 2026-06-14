package com.quare.bibleplanner.feature.login.presentation.model

/**
 * The kind of failure that can happen during a social login, used to pick the
 * message shown to the user.
 */
enum class LoginError {
    /** The request to the auth server timed out or could not reach the network. */
    CONNECTION,

    /** Any other, unexpected failure. */
    GENERIC,
}
