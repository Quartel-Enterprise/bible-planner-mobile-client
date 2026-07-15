package com.quare.bibleplanner.core.provider.supabase.session

import io.github.jan.supabase.auth.SessionManager

internal expect fun createPlatformSessionManager(): SessionManager
