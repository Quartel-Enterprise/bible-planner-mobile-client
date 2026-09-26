package com.quare.bibleplanner.core.profile.fake

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.plugins.SupabasePluginProvider
import io.github.jan.supabase.realtime.Realtime

internal class FakeRealtimeProvider(
    private val realtime: FakeRealtime,
) : SupabasePluginProvider<Realtime.Config, Realtime> {
    override val key: String = Realtime.key

    override fun createConfig(init: Realtime.Config.() -> Unit): Realtime.Config = Realtime.createConfig(init)

    override fun create(
        supabaseClient: SupabaseClient,
        config: Realtime.Config,
    ): Realtime = realtime
}
