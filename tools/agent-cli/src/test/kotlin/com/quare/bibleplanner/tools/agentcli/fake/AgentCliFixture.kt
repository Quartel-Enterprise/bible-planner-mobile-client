package com.quare.bibleplanner.tools.agentcli.fake

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.Navigator
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration
import com.quare.bibleplanner.tools.agentcli.catalog.RouteCatalog
import com.quare.bibleplanner.tools.agentcli.catalog.ScreenCatalog
import com.quare.bibleplanner.tools.agentcli.catalog.ViewModelCatalog
import com.quare.bibleplanner.tools.agentcli.command.AgentCli
import com.quare.bibleplanner.tools.agentcli.command.CommandExecutor
import com.quare.bibleplanner.tools.agentcli.command.CommandParser
import com.quare.bibleplanner.tools.agentcli.json.ArgumentDecoder
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import com.quare.bibleplanner.tools.agentcli.session.AgentSession
import com.quare.bibleplanner.tools.agentcli.session.EntryFactory
import com.quare.bibleplanner.ui.utils.AppSnackbarController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.testTimeSource
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.time.Duration.Companion.milliseconds

// An agent CLI over fake ViewModels: MainNavRoute shows HomeViewModel and SampleViewModel.
internal class AgentCliFixture(
    val cli: AgentCli,
    val session: AgentSession,
    val trackedRoutes: List<NavKey>,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun TestScope.createAgentCliFixture(
    mainDispatcher: CoroutineDispatcher,
    isWide: Boolean,
): AgentCliFixture {
    Dispatchers.setMain(mainDispatcher)
    val trackedRoutes = mutableListOf<NavKey>()
    val koin = koinApplication {
        modules(
            module {
                singleOf(::Navigator)
                singleOf(::AppSnackbarController)
                factoryOf(::HomeViewModel)
                factoryOf(::SampleViewModel)
                factory { parameters -> DayScreenViewModel(route = parameters.get(), navigator = get()) }
                factoryOf(::AppLevelViewModel)
                factory<BrokenViewModel> { error("BrokenViewModel can't be built") }
            },
        )
    }.koin
    val log = SessionLog(testTimeSource)
    val encoder = StateEncoder { resource -> "text of ${resource.key}" }
    val json = Json { serializersModule = navigationSavedStateConfiguration.serializersModule }
    val routeCatalog = RouteCatalog(
        serializersModule = navigationSavedStateConfiguration.serializersModule,
        json = json,
    )
    val screenCatalog = ScreenCatalog(
        viewModelCatalog = ViewModelCatalog.from(koin),
        viewModelsByRoute = mapOf(
            "MainNavRoute" to listOf("HomeViewModel", "SampleViewModel"),
            "ThemeNavRoute" to listOf("SampleViewModel", "AppLevelViewModel"),
            "EditNameNavRoute" to listOf("BrokenViewModel"),
        ),
        routeParameters = mapOf("ThemeNavRoute" to { MainNavRoute }),
        appViewModelNames = listOf("AppLevelViewModel"),
    )
    val session = AgentSession(
        routeCatalog = routeCatalog,
        screenCatalog = screenCatalog,
        entryFactory = EntryFactory(
            koin = koin,
            log = log,
            encoder = encoder,
        ),
        navigator = koin.get(),
        snackbarController = koin.get(),
        trackDestination = { route -> trackedRoutes += route },
        log = log,
        encoder = encoder,
        timeSource = testTimeSource,
        isWide = isWide,
    )
    withContext(Dispatchers.Main) { session.start(MainNavRoute) }
    val cli = AgentCli(
        parser = CommandParser(json),
        executor = CommandExecutor(
            session = session,
            routeCatalog = routeCatalog,
            screenCatalog = screenCatalog,
            decoder = ArgumentDecoder(json),
            encoder = encoder,
            timeSource = testTimeSource,
            initialRoute = MainNavRoute,
        ),
        session = session,
        log = log,
        settleQuiet = 50.milliseconds,
    )
    cli.run("log")
    return AgentCliFixture(
        cli = cli,
        session = session,
        trackedRoutes = trackedRoutes,
    )
}
