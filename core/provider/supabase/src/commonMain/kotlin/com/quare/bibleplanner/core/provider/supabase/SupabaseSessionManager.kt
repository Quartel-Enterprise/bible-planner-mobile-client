package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.SessionManager

internal expect fun createPlatformSessionManager(): SessionManager
