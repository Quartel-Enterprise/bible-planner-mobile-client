package com.quare.bibleplanner.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.domain.usecase.InitializeAppContent
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.ObserveSelectedVersionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

internal class InitializeAppContentUseCase(
    private val initializeBooksIfNeeded: InitializeBooksIfNeededUseCase,
    private val initializeBibleVersions: InitializeBibleVersionsUseCase,
    private val ensureStartDateIsAvailable: EnsureStartDateIsAvailableUseCase,
    private val observeSelectedVersion: ObserveSelectedVersionUseCase,
    private val remoteConfig: RemoteConfigService, // Don't delete it, it is necessary to initialize remote config
) : InitializeAppContent {
    override operator fun invoke(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            val initializeBooksDeferred = async { initializeBooksIfNeeded() }
            val initializeBibleVersionsDeferred = async { initializeBibleVersions() }
            val ensureStartDateDeferred = async { ensureStartDateIsAvailable() }
            listOf(
                initializeBooksDeferred,
                initializeBibleVersionsDeferred,
                ensureStartDateDeferred,
            ).joinAll()
            observeSelectedVersion()
        }
    }
}
