package com.quare.bibleplanner.feature.inappupdate.di

import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateDownloadViewModel
import com.quare.bibleplanner.feature.inappupdate.presentation.InAppUpdateViewModel
import com.quare.bibleplanner.feature.inappupdate.presentation.UpdateDownloadedViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureInAppUpdateModule = module {
    viewModelOf(::InAppUpdateViewModel)
    viewModelOf(::UpdateDownloadedViewModel)
    viewModelOf(::InAppUpdateDownloadViewModel)
}
