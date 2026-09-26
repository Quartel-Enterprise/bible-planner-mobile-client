package com.quare.bibleplanner.e2e.harness

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.ViewModelStoreOwner
import org.koin.core.KoinApplication
import org.koin.core.module.Module

internal interface E2ePlatform {
    val modules: List<Module>

    fun configure(koinApplication: KoinApplication)

    fun createDirectory(): String

    fun deleteDirectory(path: String)

    @OptIn(ExperimentalTestApi::class)
    fun ComposeUiTest.showApp(
        window: E2eWindow,
        viewModelStoreOwner: ViewModelStoreOwner,
    ): AutoCloseable
}

internal expect val e2ePlatform: E2ePlatform
