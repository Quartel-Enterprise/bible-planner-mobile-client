package com.quare.bibleplanner.tools.agentcli.fake

internal sealed interface SampleUiEvent {
    data class OnCount(
        val amount: Int,
    ) : SampleUiEvent

    data class OnTone(
        val tone: SampleTone,
    ) : SampleUiEvent

    data class OnRename(
        val name: String?,
    ) : SampleUiEvent

    data class OnItems(
        val items: Set<Int>,
    ) : SampleUiEvent

    data class OnContent(
        val content: SampleContent,
    ) : SampleUiEvent

    data class OnHolder(
        val holder: SampleHolder,
    ) : SampleUiEvent

    // Why: a plain class, since data classes may not have defaults and the decoder must skip optional arguments.
    class OnStep(
        val step: Int = 3,
    ) : SampleUiEvent

    data object OnReset : SampleUiEvent
}
