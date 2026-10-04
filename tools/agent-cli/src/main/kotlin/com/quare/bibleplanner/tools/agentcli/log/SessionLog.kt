package com.quare.bibleplanner.tools.agentcli.log

import kotlinx.serialization.json.JsonElement
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class SessionLog(
    private val timeSource: TimeSource,
) {
    private val entries = ArrayDeque<LogEntry>()

    @Volatile
    var lastActivity: TimeMark = timeSource.markNow()
        private set

    fun record(
        kind: LogKind,
        source: String,
        payload: JsonElement,
    ) {
        synchronized(entries) {
            entries.addLast(
                LogEntry(
                    kind = kind,
                    source = source,
                    payload = payload,
                ),
            )
        }
        touch()
    }

    fun touch() {
        lastActivity = timeSource.markNow()
    }

    fun takeEntries(): List<LogEntry> = synchronized(entries) {
        entries.toList().also { entries.clear() }
    }
}
