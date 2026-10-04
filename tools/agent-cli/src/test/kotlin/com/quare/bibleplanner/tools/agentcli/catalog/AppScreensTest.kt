package com.quare.bibleplanner.tools.agentcli.catalog

import com.quare.bibleplanner.core.model.route.ShareVerseImageNavRoute
import com.quare.bibleplanner.core.model.route.ShareVerseNavRoute
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.tools.agentcli.di.createAgentCliModules
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import com.quare.bibleplanner.tools.agentcli.storage.FilePreferencesFactory
import com.quare.bibleplanner.tools.agentcli.storage.isolateStorage
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.TimeSource

internal class AppScreensTest {
    private val directory: File = Files.createTempDirectory("agent-cli").toFile()
    private val routeCatalog = RouteCatalog(
        serializersModule = navigationSavedStateConfiguration.serializersModule,
        json = Json { serializersModule = navigationSavedStateConfiguration.serializersModule },
    )
    private val savedProperties = listOf(
        "user.home",
        "user.dir",
        "java.io.tmpdir",
        FilePreferencesFactory.ROOT_PROPERTY,
        "java.util.prefs.PreferencesFactory",
    ).associateWith(System::getProperty)
    private lateinit var screenCatalog: ScreenCatalog

    @BeforeTest
    fun setUp() {
        isolateStorage(
            dataDirectory = directory,
            isFresh = false,
            environment = emptyMap(),
        )
        initializeKoin(platformModules = createAgentCliModules(SessionLog(TimeSource.Monotonic)))
        screenCatalog = ScreenCatalog(
            viewModelCatalog = ViewModelCatalog.from(GlobalContext.get()),
            viewModelsByRoute = AppScreens.viewModelsByRoute,
            routeParameters = AppScreens.routeParameters,
            appViewModelNames = AppScreens.appViewModels,
        )
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        savedProperties.forEach { (name, value) ->
            if (value == null) System.clearProperty(name) else System.setProperty(name, value)
        }
        directory.deleteRecursively()
    }

    @Test
    fun `every route shows the ViewModels of its entry`() {
        // When
        val viewModelsByRoute = routeCatalog.routes.associate { route ->
            route.name to screenCatalog.viewModelsFor(route.kClass).map { it.simpleName }
        }

        // Then
        assertEquals(
            expected = emptyMap(),
            actual = viewModelsByRoute.filterValues(List<String?>::isEmpty),
        )
    }

    @Test
    fun `the share image sheet hands its ViewModel the verses as a share route`() {
        // Given
        val route = ShareVerseImageNavRoute(
            bookId = "PSA",
            chapterNumber = 23,
            verseNumbers = listOf(1, 2),
        )

        // When
        val parameter = screenCatalog.parameterFor(route)

        // Then
        assertEquals(
            expected = ShareVerseNavRoute(
                bookId = "PSA",
                chapterNumber = 23,
                verseNumbers = listOf(1, 2),
            ),
            actual = parameter,
        )
    }

    @Test
    fun `the app-level ViewModels are in the graph`() {
        // When
        val names = screenCatalog.appViewModels.map { it.simpleName }

        // Then
        assertEquals(
            expected = AppScreens.appViewModels,
            actual = names,
        )
    }
}
