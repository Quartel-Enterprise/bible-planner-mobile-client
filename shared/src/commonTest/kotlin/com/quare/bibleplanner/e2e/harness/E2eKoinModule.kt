package com.quare.bibleplanner.e2e.harness

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.datastore.createCommonDataStore
import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.billing.domain.model.SubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.BillingUserAccount
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetOfferingsResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetPurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.GetRestorePurchaseResultUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.InitializeBillingUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsProUserUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveInstagramLinkVisible
import com.quare.bibleplanner.core.provider.billing.domain.usecase.ObserveStoreSubscriptionStatus
import com.quare.bibleplanner.core.provider.billing.domain.usecase.TrackCustomPaywallImpression
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.crashlytics.domain.service.CrashReporter
import com.quare.bibleplanner.core.remoteconfig.domain.service.RemoteConfigDataSource
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flowOf
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private const val SUPABASE_URL = "https://e2e.supabase.co"
private const val SUPABASE_KEY = "e2e-anon-key"
private const val NO_STORE_MESSAGE = "The end-to-end tests have no store to buy from"

internal fun e2eKoinModule(
    supabaseEngine: HttpClientEngine,
    dataStoreDirectory: String,
    currentTimestamp: Long,
): Module = module {
    single<DataStore<Preferences>> {
        createCommonDataStore { dataStoreFileName -> "$dataStoreDirectory/$dataStoreFileName" }
    }
    single<CurrentTimestampProvider> { CurrentTimestampProvider { currentTimestamp } }
    single<NetworkConnectivityObserver> { NetworkConnectivityObserver { flowOf(true) } }

    single<SessionManager> { MemorySessionManager() }
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY,
        ) {
            httpEngine = supabaseEngine
            install(Auth) {
                sessionManager = get()
                enableLifecycleCallbacks = false
            }
            install(Storage)
            install(Postgrest)
            install(Functions)
            install(Realtime)
        }
    }
    single<HttpClient> {
        HttpClient(MockEngine { respondError(HttpStatusCode.NotFound) })
    }

    single<AnalyticsService> { NoOpAnalyticsService() }
    single<CrashReporter> { NoOpCrashReporter() }
    single<RemoteConfigDataSource> { DefaultValuesRemoteConfigDataSource() }

    factory<BillingUserAccount> { SignedOutBillingUserAccount() }
    factory { InitializeBillingUseCase {} }
    factory { IsProUserUseCase { false } }
    factory { ObserveStoreSubscriptionStatus { flowOf(SubscriptionStatus.Free) } }
    factory { GetOfferingsResultUseCase { Result.success(emptyList()) } }
    factory { GetPurchaseResultUseCase { Result.failure(IllegalStateException(NO_STORE_MESSAGE)) } }
    factory { GetRestorePurchaseResultUseCase { Result.failure(IllegalStateException(NO_STORE_MESSAGE)) } }
    factory { TrackCustomPaywallImpression {} }
    factory { ObserveInstagramLinkVisible { flowOf(false) } }

    single<BibleVersionDownloadNotifier> { SilentBibleVersionDownloadNotifier() }
    singleOf(::InProcessBibleVersionDownloader)
    single<BibleVersionDownloaderFacade> {
        InProcessBibleVersionDownloaderFacade(
            downloader = get(),
            pauseBibleVersion = get(),
            deleteBibleVersion = get(),
        )
    }
}
