package com.quare.bibleplanner.core.date

import android.content.Context
import com.google.android.gms.time.TrustedTime
import com.google.android.gms.time.TrustedTimeClient

internal class AndroidTrustedTimestampProvider(
    context: Context,
    private val deviceClockTimestampProvider: DeviceClockTimestampProvider,
) : CurrentTimestampProvider {
    @Volatile
    private var trustedTimeClient: TrustedTimeClient? = null

    init {
        TrustedTime
            .createClient(context)
            .addOnSuccessListener { client -> trustedTimeClient = client }
    }

    override fun getCurrentTimestamp(): Long = trustedTimeClient?.computeCurrentUnixEpochMillis()
        ?: deviceClockTimestampProvider.getCurrentTimestamp()
}
