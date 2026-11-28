package com.quare.bibleplanner.android.feature.chat

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow

/*
 * Why: the field keeps its own text so typing never waits for the view model, which also changes the text by
 * itself (a restored draft, clearing after a send). Each value the field sends comes back as state; those echoes
 * are skipped, since one can arrive after newer typing, and any other value replaces the field's text.
 */
internal class SyncedTextFieldState(
    val textFieldState: TextFieldState,
    private val currentValue: State<String>,
    private val currentOnValueChange: State<(String) -> Unit>,
) {
    private val sentValues = ArrayDeque<String>()

    // Why: an action that reads the view model's text (send, confirm) first hands it the latest typed text.
    fun sendLatest() {
        send(textFieldState.text.toString())
    }

    internal fun send(text: String) {
        if (text == sentValues.lastOrNull()) return
        if (text == currentValue.value && sentValues.isEmpty()) return
        sentValues.addLast(text)
        currentOnValueChange.value(text)
    }

    internal fun onValueChanged(value: String) {
        val echoIndex = sentValues.indexOf(value)
        if (echoIndex >= 0) {
            repeat(echoIndex + 1) { sentValues.removeFirst() }
            return
        }
        sentValues.clear()
        if (textFieldState.text.toString() != value) textFieldState.setTextAndPlaceCursorAtEnd(value)
    }
}

@Composable
internal fun rememberSyncedTextFieldState(
    value: String,
    onValueChange: (String) -> Unit,
): SyncedTextFieldState {
    val textFieldState = rememberTextFieldState(initialText = value)
    val currentValue = rememberUpdatedState(value)
    val currentOnValueChange = rememberUpdatedState(onValueChange)
    val synced = remember(textFieldState) {
        SyncedTextFieldState(
            textFieldState = textFieldState,
            currentValue = currentValue,
            currentOnValueChange = currentOnValueChange,
        )
    }
    LaunchedEffect(synced, value) {
        synced.onValueChanged(value)
    }
    LaunchedEffect(synced) {
        snapshotFlow { textFieldState.text.toString() }.collect(synced::send)
    }
    return synced
}
