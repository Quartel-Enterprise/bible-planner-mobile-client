package com.quare.bibleplanner.feature.login.presentation

/**
 * Whether the [Throwable] that aborted a Google sign-in means the platform could not hand over a
 * Google credential at all — as opposed to a failure the throwable itself explains.
 *
 * Only Android reports it: its Credential Manager answers the same way whether the device has no
 * Google account or the request was rejected (an OAuth client that does not cover this build, an
 * unhealthy Play services), and there is no permission-free API to tell the two apart. The other
 * platforms always answer `false`.
 */
fun interface IsGoogleCredentialUnavailable {
    operator fun invoke(error: Throwable?): Boolean
}
