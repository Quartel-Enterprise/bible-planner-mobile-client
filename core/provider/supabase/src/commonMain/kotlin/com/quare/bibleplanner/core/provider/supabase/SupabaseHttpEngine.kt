package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine

/**
 * Provides the Ktor engine used by the Supabase client. Pinned per platform so Supabase Realtime
 * always runs on a WebSocket-capable engine: the Android default (ktor-client-android, pulled in by
 * core/network) does not support WebSockets, which would silently break the realtime channel.
 */
internal expect fun createPlatformHttpEngine(): HttpClientEngine
