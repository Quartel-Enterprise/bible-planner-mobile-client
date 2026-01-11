package com.quare.bibleplanner.core.utils.json_reader.di

import com.quare.bibleplanner.core.utils.json_reader.JsonResourceReader
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val jsonReaderModule = module {
    single { Json { ignoreUnknownKeys = true } }
    singleOf(::JsonResourceReader)
}
