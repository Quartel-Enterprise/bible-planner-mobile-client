package com.quare.bibleplanner.tools.agentcli.command

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlin.time.Duration.Companion.milliseconds

class CommandParser(
    private val json: Json,
) {
    private val defaultQuiet = 300.milliseconds
    private val defaultWaitTimeout = 10_000.milliseconds

    // Why: only options at the end are options, so a value like "Day 1 -- Genesis" stays whole.
    private val trailingWaitOption = Regex("""\s+--(?:app|timeout\s+\d+)\s*$""")

    fun parse(line: String): Command {
        val trimmed = line.trim()
        val name = trimmed.substringBefore(' ').lowercase()
        val rest = trimmed.substringAfter(' ', missingDelimiterValue = "").trim()
        return when (name) {
            "help", "?" -> Command.Help

            "routes" -> Command.Routes(filter = rest.ifBlank { null })

            "open", "replace" -> {
                val (route, arguments) = splitArguments(rest)
                Command.Open(
                    route = route.requireValue("$name <Route> [{json}]"),
                    arguments = arguments,
                    isReplacingTop = name == "replace",
                )
            }

            "back" -> Command.Back

            "reset" -> Command.Reset

            "stack" -> Command.Stack

            "state" -> {
                val options = Options.parse(rest)
                Command.State(
                    path = options.positional.firstOrNull(),
                    maxItems = options.getInt(LIMIT) ?: DEFAULT_LIMIT,
                    isApp = APP in options.flags,
                )
            }

            "events" -> Command.Events(viewModel = rest.ifBlank { null })

            "event" -> {
                val (target, arguments) = splitArguments(rest)
                Command.Event(
                    target = target.requireValue("event [ViewModel.]Event [{json}]"),
                    arguments = arguments,
                )
            }

            "functions" -> Command.Functions(viewModel = rest.ifBlank { null })

            "call" -> {
                val (target, arguments) = splitArguments(rest)
                Command.Call(
                    target = target.requireValue("call ViewModel.function [{json}]"),
                    arguments = arguments,
                    maxItems = DEFAULT_LIMIT,
                )
            }

            "settle" -> Command.Settle(quiet = rest.toLongOrNull()?.milliseconds ?: defaultQuiet)

            "wait" -> parseWait(rest)

            "log" -> Command.Log

            "quit", "exit" -> Command.Quit

            else -> throw IllegalArgumentException("unknown command $name; run `help`")
        }
    }

    private fun parseWait(rest: String): Command.Wait {
        var condition = rest
        while (true) {
            val option = trailingWaitOption.find(condition) ?: break
            condition = condition.removeRange(option.range)
        }
        val options = Options.parse(rest.removePrefix(condition))
        val path = condition.substringBefore('=').trim()
        val expected = condition
            .substringAfter('=', missingDelimiterValue = "")
            .trim()
            .takeIf(String::isNotEmpty)
            ?.let(::parseValue)
        return Command.Wait(
            path = path.requireValue("wait <path>[ = <json>] [--timeout ms] [--app]"),
            expected = expected,
            timeout = options.getInt(TIMEOUT)?.milliseconds ?: defaultWaitTimeout,
            isApp = APP in options.flags,
        )
    }

    // Why: `wait path = Loaded` reads better than `= "Loaded"`, so a bare word is taken as text.
    private fun parseValue(value: String): JsonElement {
        val element = runCatching { json.parseToJsonElement(value) }.getOrNull()
        val isBareWord = element == null ||
            (
                element is JsonPrimitive &&
                    element !is JsonNull &&
                    !element.isString &&
                    element.booleanOrNull == null &&
                    element.doubleOrNull == null
            )
        return if (isBareWord) JsonPrimitive(value) else element
    }

    private fun splitArguments(rest: String): Pair<String, JsonObject> {
        val jsonStart = rest.indexOf('{')
        if (jsonStart < 0) return rest.trim() to JsonObject(emptyMap())
        val element: JsonElement = json.parseToJsonElement(rest.substring(jsonStart))
        require(element is JsonObject) { "arguments must be a JSON object" }
        return rest.substring(0, jsonStart).trim() to element
    }

    private fun String.requireValue(usage: String): String {
        require(isNotBlank()) { "usage: $usage" }
        return this
    }

    private class Options(
        val positional: List<String>,
        val flags: Set<String>,
        private val values: Map<String, String>,
    ) {
        fun getInt(name: String): Int? = values[name]?.let { value ->
            requireNotNull(value.toIntOrNull()) { "--$name takes a number" }
        }

        companion object {
            fun parse(text: String): Options {
                val tokens = text.split(' ').filter(String::isNotBlank)
                val positional = mutableListOf<String>()
                val flags = mutableSetOf<String>()
                val values = mutableMapOf<String, String>()
                var index = 0
                while (index < tokens.size) {
                    val token = tokens[index]
                    val flag = token.removePrefix("--")
                    when {
                        !token.startsWith("--") -> positional += token

                        flag == LIMIT || flag == TIMEOUT -> values[flag] =
                            requireNotNull(tokens.getOrNull(++index)) { "--$flag takes a value" }

                        else -> flags += flag
                    }
                    index++
                }
                return Options(
                    positional = positional,
                    flags = flags,
                    values = values,
                )
            }
        }
    }

    private companion object {
        const val DEFAULT_LIMIT = 50
        const val LIMIT = "limit"
        const val TIMEOUT = "timeout"
        const val APP = "app"
    }
}
