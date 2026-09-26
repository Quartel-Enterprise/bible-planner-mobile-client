package com.quare.bibleplanner.e2e.harness

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.waitUntilDoesNotExist

// The app reads Room and DataStore off the main thread, which waitForIdle does not wait for, so every
// step waits for the node it needs instead.
private const val TIMEOUT_MILLIS = 10_000L

// A flow that fails says which screen it was on: the error carries the text of every node on screen.
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitNode(matcher: SemanticsMatcher): SemanticsNodeInteraction {
    try {
        waitUntilAtLeastOneExists(
            matcher = matcher,
            timeoutMillis = TIMEOUT_MILLIS,
        )
    } catch (timeout: ComposeTimeoutException) {
        throw AssertionError("${timeout.message}\nOn screen:\n${screenText()}", timeout)
    }
    return onAllNodes(matcher).onFirst()
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.screenText(): String = onAllNodes(isRoot())
    .fetchSemanticsNodes()
    .flatMap { root -> root.textsInTree() }
    .joinToString(separator = "\n")

private fun SemanticsNode.textsInTree(): List<String> {
    val texts = config.getOrNull(SemanticsProperties.Text).orEmpty().map { text -> text.text } +
        config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
    return texts + children.flatMap { child -> child.textsInTree() }
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitText(text: String): SemanticsNodeInteraction = awaitNode(hasText(text))

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.clickText(text: String) {
    click(hasText(text))
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.clickDescription(description: String) {
    click(hasContentDescription(description))
}

// A phone in landscape is short enough to leave most lists below the fold, and a click outside the
// window never reaches the app, so a node in a scrollable container is scrolled into view first.
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.click(matcher: SemanticsMatcher) {
    val node = awaitNode(matcher)
    val isInScrollableContainer = generateSequence(node.fetchSemanticsNode().parent) { ancestor -> ancestor.parent }
        .any { ancestor -> SemanticsActions.ScrollBy in ancestor.config }
    if (isInScrollableContainer) {
        node.performScrollTo()
    }
    node.performClick()
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitGone(matcher: SemanticsMatcher) {
    waitUntilDoesNotExist(
        matcher = matcher,
        timeoutMillis = TIMEOUT_MILLIS,
    )
}
