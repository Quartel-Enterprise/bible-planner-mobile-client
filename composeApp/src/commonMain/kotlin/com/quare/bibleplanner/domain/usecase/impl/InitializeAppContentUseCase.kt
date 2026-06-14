package com.quare.bibleplanner.domain.usecase.impl

import com.quare.bibleplanner.core.books.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.EnsureDefaultPlanStartDateUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.MigratePlanPreferencesToSyncStoreUseCase
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigService
import com.quare.bibleplanner.core.sync.domain.usecase.ObserveSync
import com.quare.bibleplanner.domain.usecase.InitializeAppContent
import com.quare.bibleplanner.feature.applanguage.domain.usecase.ObserveAppLocale
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.ObserveSelectedVersionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

internal class InitializeAppContentUseCase(
    private val initializeBooksIfNeeded: InitializeBooksIfNeededUseCase,
    private val initializeBibleVersions: InitializeBibleVersionsUseCase,
    private val migratePlanPreferencesToSyncStore: MigratePlanPreferencesToSyncStoreUseCase,
    private val ensureDefaultPlanStartDate: EnsureDefaultPlanStartDateUseCase,
    private val observeSelectedVersion: ObserveSelectedVersionUseCase,
    private val observeAppLocale: ObserveAppLocale,
    private val observeSync: ObserveSync,
    private val remoteConfig: RemoteConfigService, // Don't delete it, it is necessary to initialize remote config
) : InitializeAppContent {
    override operator fun invoke(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            // Move legacy DataStore plan preferences into the synced store before anything reads them.
            migratePlanPreferencesToSyncStore()
            val initializeBooksDeferred = async { initializeBooksIfNeeded() }
            val initializeBibleVersionsDeferred = async { initializeBibleVersions() }
            val ensureStartDateDeferred = async { ensureDefaultPlanStartDate() }
            listOf(
                initializeBooksDeferred,
                initializeBibleVersionsDeferred,
                ensureStartDateDeferred,
            ).joinAll()
            launch { observeAppLocale() }
            // Launched after book rows exist so remote favorites can be applied to them.
            launch { observeSync() }
            observeSelectedVersion()
        }
    }
}
