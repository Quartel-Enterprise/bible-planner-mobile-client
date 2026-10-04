package com.quare.bibleplanner.tools.agentcli.log

import kotlinx.serialization.json.JsonElement

data class LogEntry(
    val kind: LogKind,
    val source: String,
    val payload: JsonElement,
)
