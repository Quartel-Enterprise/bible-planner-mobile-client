package com.quare.bibleplanner.core.clear.domain

/**
 * Wipes the locally cached AI chat. The conversations are personal and only kept offline as a
 * mirror of the account's, so they must not survive a logout: a different account on the same
 * device would otherwise read the previous one's history. Implemented by the chat feature.
 */
fun interface ClearChatLocalData {
    suspend operator fun invoke()
}
