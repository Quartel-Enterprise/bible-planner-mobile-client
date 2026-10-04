package com.quare.bibleplanner.tools.agentcli.cli

import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class AgentCliOptionsParser(
    private val defaultDataDirectory: File,
) {
    private val knownFlags = setOf(FRESH, SERVE, WIDE, COMPACT)
    private val knownValues = setOf(DATA_DIR, PORT, PORT_FILE, SETTLE_MS)

    fun parse(arguments: List<String>): AgentCliOptions {
        val flags = mutableSetOf<String>()
        val values = mutableMapOf<String, String>()
        var index = 0
        while (index < arguments.size) {
            val name = arguments[index].removePrefix("--")
            require(arguments[index].startsWith("--") && name in knownFlags + knownValues) {
                "unknown option ${arguments[index]}; options: ${(knownFlags + knownValues).joinToString { "--$it" }}"
            }
            if (name in knownValues) {
                values[name] = requireNotNull(arguments.getOrNull(++index)) { "--$name takes a value" }
            } else {
                flags += name
            }
            index++
        }
        return AgentCliOptions(
            dataDirectory = values[DATA_DIR]?.let(::File) ?: defaultDataDirectory,
            isFresh = FRESH in flags,
            isServing = SERVE in flags,
            port = values[PORT]?.let { port -> requireNotNull(port.toIntOrNull()) { "--$PORT takes a number" } } ?: 0,
            portFile = values[PORT_FILE]?.let(::File),
            isWide = WIDE in flags,
            isCompact = COMPACT in flags,
            settleQuiet = (
                values[SETTLE_MS]?.let { value ->
                    requireNotNull(value.toLongOrNull()) { "--$SETTLE_MS takes a number" }
                }
                    ?: DEFAULT_SETTLE_MS
            ).milliseconds,
        )
    }

    private companion object {
        const val DATA_DIR = "data-dir"
        const val FRESH = "fresh"
        const val SERVE = "serve"
        const val PORT = "port"
        const val PORT_FILE = "port-file"
        const val WIDE = "wide"
        const val COMPACT = "compact"
        const val SETTLE_MS = "settle-ms"
        const val DEFAULT_SETTLE_MS = 300L
    }
}
