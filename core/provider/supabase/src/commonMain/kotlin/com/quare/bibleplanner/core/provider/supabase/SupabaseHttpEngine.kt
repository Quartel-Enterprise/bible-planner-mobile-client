package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine

// Why: pinned per platform so Realtime gets a WebSocket-capable engine; the Android
// default ktor-client-android has no WebSockets and silently breaks the channel.
internal expect fun createPlatformHttpEngine(): HttpClientEngine
