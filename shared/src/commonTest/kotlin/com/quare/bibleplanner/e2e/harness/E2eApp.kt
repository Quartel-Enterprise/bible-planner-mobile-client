package com.quare.bibleplanner.e2e.harness

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBibleVersionsUseCase
import com.quare.bibleplanner.core.books.domain.usecase.InitializeBooksIfNeededUseCase
import com.quare.bibleplanner.core.utils.coroutines.ApplicationScope
import com.quare.bibleplanner.core.utils.locale.Language
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.domain.usecase.SetAppLanguage
import com.quare.bibleplanner.feature.bibleversion.domain.DownloadBibleUseCase
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.bibleversion.domain.usecase.SetSelectedVersionUseCase
import kotlinx.coroutines.cancel
import org.koin.core.Koin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform

// The whole app, as a user who has just installed it and opened it once: the books are seeded, the
// language is English and the default Bible version is downloaded. Only the outside world is fake.
internal class E2eApp {
    private val directory: String = e2ePlatform.createDirectory()
    private val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }
    private var shownApp: AutoCloseable? = null

    @OptIn(ExperimentalTestApi::class)
    suspend fun ComposeUiTest.launch(
        window: E2eWindow,
        arrange: suspend Koin.() -> Unit,
    ) {
        initializeKoin(
            platformModules = e2ePlatform.modules + e2eKoinModule(
                supabaseEngine = fakeSupabaseEngine(),
                dataStoreDirectory = directory,
                currentTimestamp = CURRENT_TIMESTAMP,
            ),
            config = { e2ePlatform.configure(koinApplication = this) },
        )
        val koin = KoinPlatform.getKoin()
        koin.completeFirstLaunch()
        koin.arrange()
        shownApp = with(e2ePlatform) {
            showApp(
                window = window,
                viewModelStoreOwner = viewModelStoreOwner,
            )
        }
    }

    // The in-memory database is left open: a screen that is still being cancelled may be halfway
    // through a query, and closing it under that query fails the next test with an uncaught
    // exception. It goes away with the rest of the graph.
    fun stop() {
        shownApp?.close()
        viewModelStoreOwner.viewModelStore.clear()
        KoinPlatform.getKoinOrNull()?.let { koin ->
            koin.get<InProcessBibleVersionDownloader>().cancelAllDownloads()
            koin.get<ApplicationScope>().cancel()
        }
        stopKoin()
        e2ePlatform.deleteDirectory(directory)
    }

    private suspend fun Koin.completeFirstLaunch() {
        get<InitializeBooksIfNeededUseCase>()()
        get<InitializeBibleVersionsUseCase>()()
        get<SetAppLanguage>()(Language.ENGLISH)
        get<SetSelectedVersionUseCase>()(FakeBibles.englishDefault.id)
        get<DownloadBibleUseCase>()(FakeBibles.englishDefault.id).getOrThrow()
    }

    private companion object {
        const val CURRENT_TIMESTAMP = 1_772_452_800_000L
    }
}
