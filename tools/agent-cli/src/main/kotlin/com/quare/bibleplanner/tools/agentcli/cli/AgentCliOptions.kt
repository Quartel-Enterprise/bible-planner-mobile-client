package com.quare.bibleplanner.tools.agentcli.cli

import java.io.File
import kotlin.time.Duration

data class AgentCliOptions(
    val dataDirectory: File,
    val isFresh: Boolean,
    val isServing: Boolean,
    val port: Int,
    val portFile: File?,
    val isWide: Boolean,
    val isCompact: Boolean,
    val settleQuiet: Duration,
)
