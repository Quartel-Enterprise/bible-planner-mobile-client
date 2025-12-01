package com.quare.bibleplanner.core.datastore.di

import com.quare.bibleplanner.core.datastore.createDataStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataStoreProviderModule = module { singleOf(::createDataStore) }
