package com.quare.bibleplanner.tools.agentcli.command

import com.quare.bibleplanner.core.utils.suspendRunCatching
import com.quare.bibleplanner.tools.agentcli.log.LogEntry
import com.quare.bibleplanner.tools.agentcli.log.SessionLog
import com.quare.bibleplanner.tools.agentcli.session.AgentSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.lang.reflect.InvocationTargetException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class AgentCli(
    private val parser: CommandParser,
    private val executor: CommandExecutor,
    private val session: AgentSession,
    private val log: SessionLog,
    private val settleQuiet: Duration,
) {
    private val settleTimeout = 10_000.milliseconds

    suspend fun run(line: String): Response {
        val start = TimeSource.Monotonic.markNow()
        val command = runCatching { parser.parse(line) }.getOrElse { error ->
            return Response(
                json = buildFailure(error),
                isQuit = false,
            )
        }
        val result = suspendRunCatching {
            withContext(Dispatchers.Main) {
                val result = executor.execute(command)
                // Why: the quiet window starts with the command, or an effect still on its way counts as settled.
                log.touch()
                val isSettled = !command.changesScreen ||
                    session.awaitSettled(
                        quiet = settleQuiet,
                        timeout = settleTimeout,
                    )
                // Why: navigation reaches the back stack asynchronously, so the screen is read once it settled.
                val shown = if (command.changesScreen && command !is Command.Call) executor.getScreen() else result
                shown to isSettled
            }
        }
        val json = result.fold(
            onSuccess = { (value, isSettled) ->
                buildJsonObject {
                    put("ok", true)
                    put("result", value)
                    if (!isSettled) put("settled", false)
                    put("log", log.takeEntries().toJson())
                    put("ms", start.elapsedNow().inWholeMilliseconds)
                }
            },
            onFailure = ::buildFailure,
        )
        return Response(
            json = json,
            isQuit = command == Command.Quit,
        )
    }

    private fun buildFailure(error: Throwable): JsonElement {
        val cause = (error as? InvocationTargetException)?.targetException ?: error
        // Why: Koin wraps the error that says what went wrong, and coroutines copy errors they rethrow.
        val rootCause = generateSequence(cause, Throwable::cause).last()
        val message = listOfNotNull(cause.message ?: cause.toString(), rootCause.message)
            .distinct()
            .joinToString(separator = ": ")
        return buildJsonObject {
            put("ok", false)
            put("error", message)
            put("log", log.takeEntries().toJson())
        }
    }

    private fun List<LogEntry>.toJson(): JsonArray = JsonArray(
        map { entry ->
            JsonObject(
                mapOf(
                    "kind" to JsonPrimitive(entry.kind.name.lowercase()),
                    "source" to JsonPrimitive(entry.source),
                    "payload" to entry.payload,
                ),
            )
        },
    )

    class Response(
        val json: JsonElement,
        val isQuit: Boolean,
    )
}
