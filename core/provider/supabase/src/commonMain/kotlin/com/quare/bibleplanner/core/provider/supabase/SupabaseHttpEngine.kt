package com.quare.bibleplanner.core.provider.supabase

import io.ktor.client.engine.HttpClientEngine

internal expect fun createPlatformHttpEngine(): HttpClientEngine
