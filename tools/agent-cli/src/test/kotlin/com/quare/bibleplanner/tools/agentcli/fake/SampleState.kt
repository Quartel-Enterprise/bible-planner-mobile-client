package com.quare.bibleplanner.tools.agentcli.fake

import kotlin.time.Duration

internal data class SampleState(
    val title: String,
    val content: SampleContent,
    val tone: SampleTone,
    val counts: Map<String, Int>,
    val onClick: () -> Unit,
    val elapsed: Duration,
    val holder: SampleHolder,
    val id: SampleId,
)
