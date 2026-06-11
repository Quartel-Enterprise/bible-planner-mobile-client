package com.quare.bibleplanner.core.provider.supabase.di

import com.quare.bibleplanner.core.provider.supabase.SupabaseDeeplinkHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val androidSupabaseModule = module {
    factoryOf(::SupabaseDeeplinkHandler)
}
