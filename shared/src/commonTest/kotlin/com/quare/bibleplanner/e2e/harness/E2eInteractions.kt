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
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.waitUntilDoesNotExist
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// The app reads Room and DataStore off the main thread, which waitForIdle does not wait for, so every
// step waits for the node it needs instead.
private const val TIMEOUT_MILLIS = 10_000L
private val settleTime = 2.seconds

// A flow that fails says which screen it was on: the error carries the text of every node on screen.
// A lazy list doesn't even compose the items below the fold, so a node that stays missing is looked
// for in the lazy lists on screen, once the screen has had time to settle: scrolling the screen a
// transition is leaving keeps that transition from finishing.
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitNode(matcher: SemanticsMatcher): SemanticsNodeInteraction {
    val start = TimeSource.Monotonic.markNow()
    try {
        waitUntil(
            conditionDescription = "at least one node matches (${matcher.description})",
            timeoutMillis = TIMEOUT_MILLIS,
        ) {
            onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() ||
                (start.elapsedNow() > settleTime && scrollLazyListsTo(matcher))
        }
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
    val isInScrollableContainer = node.fetchSemanticsNode().ancestors().any { ancestor -> ancestor.isScrollable }
    if (isInScrollableContainer) {
        node.performScrollTo()
    } else {
        revealHiddenBar(
            node = node,
            matcher = matcher,
        )
    }
    node.performClick()
}

// A bar that hides while the content scrolls down, like the read screen's top bar, keeps its nodes
// with no size, and a click on them lands nowhere. Scrolling to a node can hide one: the click on a
// verse below the fold hides the Back button of a phone in landscape. The user scrolls back up to
// bring the bar back, so the lists of the node's own screen (the nearest ancestor that has any) are
// swiped down until it shows, leaving the other panes alone. Not inside a sheet or a dialog, which a
// swipe down can dismiss. A node that still doesn't show fails here rather than taking the click.
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.revealHiddenBar(
    node: SemanticsNodeInteraction,
    matcher: SemanticsMatcher,
) {
    if (node.isDisplayed()) return
    val ancestors = node.fetchSemanticsNode().ancestors()
    if (ancestors.none { ancestor -> ancestor.isDialogOrPopup }) {
        val screenListIds = ancestors
            .map { ancestor -> ancestor.scrollableDescendantIds() }
            .firstOrNull { listIds -> listIds.isNotEmpty() }
            .orEmpty()
        val lists = onAllNodes(hasScrollAction())
        lists
            .fetchSemanticsNodes()
            .withIndex()
            .filter { (_, list) -> list.id in screenListIds }
            .forEach { (index, _) ->
                if (node.isDisplayed()) return
                lists[index].performTouchInput { swipeDown() }
            }
    }
    if (!node.isDisplayed()) {
        throw AssertionError("The node (${matcher.description}) has no size to click\nOn screen:\n${screenText()}")
    }
}

private fun SemanticsNode.ancestors(): Sequence<SemanticsNode> =
    generateSequence(parent) { ancestor -> ancestor.parent }

private val SemanticsNode.isScrollable: Boolean
    get() = SemanticsActions.ScrollBy in config

private val SemanticsNode.isDialogOrPopup: Boolean
    get() = SemanticsProperties.IsDialog in config || SemanticsProperties.IsPopup in config

private fun SemanticsNode.scrollableDescendantIds(): List<Int> = children.flatMap { child ->
    listOfNotNull(child.id.takeIf { child.isScrollable }) + child.scrollableDescendantIds()
}

// Only the lists of the topmost root are scrolled: with a dialog open, the screen behind it isn't
// the user's to scroll, and scrolling it hides the bottom bar.
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.scrollLazyListsTo(matcher: SemanticsMatcher): Boolean {
    val topRoot = onAllNodes(isRoot()).fetchSemanticsNodes().lastOrNull()?.root
    val lazyLists = onAllNodes(hasScrollToNodeAction())
    return lazyLists
        .fetchSemanticsNodes()
        .withIndex()
        .filter { (_, lazyList) -> lazyList.root == topRoot }
        .any { (index, _) -> runCatching { lazyLists[index].performScrollToNode(matcher) }.isSuccess }
}

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.awaitGone(matcher: SemanticsMatcher) {
    waitUntilDoesNotExist(
        matcher = matcher,
        timeoutMillis = TIMEOUT_MILLIS,
    )
}
