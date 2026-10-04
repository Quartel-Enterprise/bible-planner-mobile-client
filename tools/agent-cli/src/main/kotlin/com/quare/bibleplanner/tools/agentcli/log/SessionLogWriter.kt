package com.quare.bibleplanner.tools.agentcli.log

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class SessionLogWriter(
    private val log: SessionLog,
) : LogWriter() {
    override fun isLoggable(
        tag: String,
        severity: Severity,
    ): Boolean = severity >= Severity.Warn

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        log.record(
            kind = LogKind.LOG,
            source = tag,
            payload = buildJsonObject {
                put("severity", JsonPrimitive(severity.name))
                put("message", JsonPrimitive(message))
                throwable?.let { error -> put("error", JsonPrimitive(error.toString())) }
            },
        )
    }
}
