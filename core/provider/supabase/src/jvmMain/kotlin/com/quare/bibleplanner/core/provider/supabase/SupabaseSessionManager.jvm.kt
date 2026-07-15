package com.quare.bibleplanner.core.provider.supabase

import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.SettingsSessionManager

internal actual fun createPlatformSessionManager(): SessionManager = SettingsSessionManager()
