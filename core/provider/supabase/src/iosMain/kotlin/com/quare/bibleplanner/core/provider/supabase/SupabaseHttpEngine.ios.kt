package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun createPlatformHttpEngine(): HttpClientEngine = Darwin.create()
