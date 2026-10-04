package com.quare.bibleplanner.tools.agentcli

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.quare.bibleplanner.core.model.route.MainNavRoute
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration
import com.quare.bibleplanner.di.initializeKoin
import com.quare.bibleplanner.feature.applanguage.presentation.initAppLocale
import com.quare.bibleplanner.tools.agentcli.catalog.AppScreens
import com.quare.bibleplanner.tools.agentcli.catalog.RouteCatalog
import com.quare.bibleplanner.tools.agentcli.catalog.ScreenCatalog
import com.quare.bibleplanner.tools.agentcli.catalog.ViewModelCatalog
import com.quare.bibleplanner.tools.agentcli.cli.AgentCliOptionsParser
import com.quare.bibleplanner.tools.agentcli.command.AgentCli
import com.quare.bibleplanner.tools.agentcli.command.CommandExecutor
import com.quare.bibleplanner.tools.agentcli.command.CommandParser
import com.quare.bibleplanner.tools.agentcli.di.createAgentCliModules
import com.quare.bibleplanner.tools.agentcli.json.ArgumentDecoder
import com.quare.bibleplanner.tools.agentcli.json.StateEncoder
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import com.quare.bibleplanner.tools.agentcli.log.SessionLogWriter
import com.quare.bibleplanner.tools.agentcli.server.AgentCliServer
import com.quare.bibleplanner.tools.agentcli.server.Repl
import com.quare.bibleplanner.tools.agentcli.session.AgentSession
import com.quare.bibleplanner.tools.agentcli.session.EntryFactory
import com.quare.bibleplanner.tools.agentcli.storage.isolateStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.koin.core.context.GlobalContext
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

fun main(arguments: Array<String>) {
    val options = AgentCliOptionsParser(
        defaultDataDirectory = File(System.getProperty("java.io.tmpdir"), "bibleplanner-agent-cli"),
    ).parse(arguments.toList())
    // Why: stdout carries only command responses; the app's own logging would corrupt the JSON.
    val responses = PrintStream(FileOutputStream(FileDescriptor.out), true, Charsets.UTF_8)
    System.setOut(System.err)
    isolateStorage(
        dataDirectory = options.dataDirectory,
        isFresh = options.isFresh,
        environment = System.getenv(),
    )

    val log = SessionLog(TimeSource.Monotonic)
    Logger.setLogWriters(SessionLogWriter(log), platformLogWriter())
    initializeKoin(platformModules = createAgentCliModules(log))
    val koin = GlobalContext.get()
    runBlocking {
        initAppLocale(
            getAppLanguageFlow = koin.get(),
            applyLocale = koin.get(),
        )
    }

    val json = Json {
        serializersModule = navigationSavedStateConfiguration.serializersModule
        isLenient = true
    }
    val encoder = StateEncoder { resource -> runBlocking { getString(resource) } }
    val decoder = ArgumentDecoder(json)
    val routeCatalog = RouteCatalog(
        serializersModule = navigationSavedStateConfiguration.serializersModule,
        json = json,
    )
    val screenCatalog = ScreenCatalog(
        viewModelCatalog = ViewModelCatalog.from(koin),
        viewModelsByRoute = AppScreens.viewModelsByRoute,
        routeParameters = AppScreens.routeParameters,
        appViewModelNames = AppScreens.appViewModels,
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
        trackDestination = koin.get(),
        log = log,
        encoder = encoder,
        timeSource = TimeSource.Monotonic,
        isWide = options.isWide,
    )
    runBlocking(Dispatchers.Main) {
        session.start(MainNavRoute)
        session.awaitSettled(
            quiet = 500.milliseconds,
            timeout = 30.seconds,
        )
    }
    val cli = AgentCli(
        parser = CommandParser(json),
        executor = CommandExecutor(
            session = session,
            routeCatalog = routeCatalog,
            screenCatalog = screenCatalog,
            decoder = decoder,
            encoder = encoder,
            timeSource = TimeSource.Monotonic,
            initialRoute = MainNavRoute,
        ),
        session = session,
        log = log,
        settleQuiet = options.settleQuiet,
    )
    val output = Json { prettyPrint = !options.isCompact }

    if (options.isServing) {
        val port = AgentCliServer(
            cli = cli,
            output = output,
            onQuit = { exitProcess(0) },
        ).start(options.port)
        options.portFile?.writeText(port.toString())
        System.err.println("agent-cli ready on http://127.0.0.1:$port (data: ${options.dataDirectory})")
    } else {
        System.err.println("agent-cli ready (data: ${options.dataDirectory}); type `help`")
        Repl(
            cli = cli,
            output = output,
            input = System.`in`.bufferedReader(),
            responses = responses,
        ).run()
        exitProcess(0)
    }
}
