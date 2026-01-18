package com.quare.bibleplanner.core.user.di

import com.quare.bibleplanner.core.user.data.mapper.SessionUserMapper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val userModule = module {
    factoryOf(::SessionUserMapper)
}
