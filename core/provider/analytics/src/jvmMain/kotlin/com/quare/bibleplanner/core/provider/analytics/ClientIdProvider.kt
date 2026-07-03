package com.quare.bibleplanner.core.provider.analytics

import java.util.UUID
import java.util.prefs.Preferences

internal class ClientIdProvider {
    private val preferences = Preferences.userRoot().node(PREFERENCES_NODE)

    fun getClientId(): String = preferences.get(CLIENT_ID_KEY, null) ?: createClientId()

    private fun createClientId(): String = UUID.randomUUID().toString().also { clientId ->
        preferences.put(CLIENT_ID_KEY, clientId)
    }

    companion object {
        private const val PREFERENCES_NODE = "com/quare/bibleplanner/analytics"
        private const val CLIENT_ID_KEY = "ga_client_id"
    }
}
