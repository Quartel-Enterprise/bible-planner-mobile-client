package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

internal actual fun createPlatformHttpEngine(): HttpClientEngine = Js.create()
