package com.quare.bibleplanner.core.provider.koin

import androidx.room3.RoomDatabase
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloadNotifier
import com.quare.bibleplanner.core.books.domain.BibleVersionDownloaderFacade
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.route.AddNotesFreeWarningNavRoute
import com.quare.bibleplanner.core.model.route.BookDetailsNavRoute
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.CropPhotoNavRoute
import com.quare.bibleplanner.core.model.route.DayNavRoute
import com.quare.bibleplanner.core.model.route.DayReadingCompleteNavRoute
import com.quare.bibleplanner.core.model.route.DayStudyNavRoute
import com.quare.bibleplanner.core.model.route.DeleteHighlightColorNavRoute
import com.quare.bibleplanner.core.model.route.DeleteNotesRoute
import com.quare.bibleplanner.core.model.route.DeleteVersionNavRoute
import com.quare.bibleplanner.core.model.route.InAppUpdateNavRoute
import com.quare.bibleplanner.core.model.route.LoginWarningNavRoute
import com.quare.bibleplanner.core.model.route.PaywallNavRoute
import com.quare.bibleplanner.core.model.route.PaywallTeaserNavRoute
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.RenameDeviceNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.VerseNoteNavRoute
import com.quare.bibleplanner.core.provider.language.di.jvmLanguageProviderModule
import com.quare.bibleplanner.core.provider.language.di.languageProviderModule
import com.quare.bibleplanner.feature.applanguage.di.jvmAppLanguageModule
import com.quare.bibleplanner.feature.bibleversion.domain.InProcessBibleVersionDownloader
import com.quare.bibleplanner.feature.login.di.jvmLoginModule
import io.github.jan.supabase.functions.Functions
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.time.Duration

internal class CommonKoinGraphTest {
    private val hostProvidedTypes: List<KClass<*>> = listOf(
        RoomDatabase.Builder::class,
        BibleVersionDownloadNotifier::class,
        InProcessBibleVersionDownloader::class,
        BibleVersionDownloaderFacade::class,
    )
    private val inlineArgumentTypes: List<KClass<*>> = listOf(
        Functions.Config::class,
        Duration::class,
        HttpClientEngine::class,
        CoroutineScope::class,
    )
    private val navigationParameterTypes: List<KClass<*>> = listOf(
        AddNotesFreeWarningNavRoute::class,
        BookDetailsNavRoute::class,
        ChatNavRoute::class,
        CropPhotoNavRoute::class,
        DayNavRoute::class,
        DayReadingCompleteNavRoute::class,
        DayStudyNavRoute::class,
        DeleteHighlightColorNavRoute::class,
        DeleteNotesRoute::class,
        DeleteVersionNavRoute::class,
        InAppUpdateNavRoute::class,
        LoginWarningNavRoute::class,
        PlanDayLocationModel::class,
        PaywallNavRoute::class,
        PaywallTeaserNavRoute::class,
        ReadNavRoute::class,
        RenameDeviceNavRoute::class,
        ShareVerseNavRoute::class,
        VerseNoteNavRoute::class,
    )

    @Test
    fun `GIVEN the desktop koin graph WHEN verifying it THEN every dependency has a definition`() {
        // Given
        val desktopGraph = module {
            includes(
                CommonKoinUtils.modules + listOf(
                    jvmAppLanguageModule,
                    jvmLanguageProviderModule,
                    jvmLoginModule,
                    languageProviderModule,
                ),
            )
        }

        // When / Then
        desktopGraph.verify(extraTypes = hostProvidedTypes + navigationParameterTypes + inlineArgumentTypes)
    }
}
