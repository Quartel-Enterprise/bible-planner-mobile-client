package com.quare.bibleplanner.tools.agentcli.fake

internal sealed interface SampleContent {
    data object Loading : SampleContent

    data class Loaded(
        val items: List<Int>,
        val label: String?,
    ) : SampleContent
}
