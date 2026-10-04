package com.quare.bibleplanner.baselineprofile

import android.graphics.Rect
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.quare.bibleplanner"
internal const val ITERATIONS = 10

// Why: the journeys find screens only by these test tags, which MainActivity exposes as resource
// ids, so they run on a device in any language. They mirror the tags declared in the features.
private const val PLANS_LIST_TAG = "plans_list"
private const val PLAN_DAY_TAG = "plan_day"
private const val PLANS_TAB_TAG = "plans_tab"
private const val BOOKS_TAB_TAG = "books_tab"
private const val DAY_CONTENT_TAG = "day_content"
private const val DAY_PASSAGES_TAG = "day_passages"
private const val READ_CHAPTERS_TAG = "read_chapters"
private const val BOOKS_CONTENT_TAG = "books_content"
private const val TIMEOUT_MILLIS = 15_000L
private const val FLINGS = 3
private const val MAX_SCROLLS_TO_A_DAY = 5
private const val SCROLL_TO_A_DAY_PERCENT = 0.4f
private const val MAX_BACK_PRESSES = 3
private const val MAX_SCROLLS_TO_REVEAL_THE_BAR = 3
private const val SCROLL_TO_REVEAL_THE_BAR_PERCENT = 0.2f
private const val BAR_SETTLE_MILLIS = 1_000L
private const val BOTTOM_BAR_MIN_TOP_RATIO = 0.75

internal fun MacrobenchmarkScope.startAndWaitForPlans() {
    device.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
    pressHome()
    startActivityAndWait()
    device.findWhenShown(By.res(PLANS_LIST_TAG).hasDescendant(By.res(PLAN_DAY_TAG)))
    checkNarrowLayout()
}

internal fun MacrobenchmarkScope.scrollPlans() {
    device.findWhenShown(By.res(PLANS_LIST_TAG)).flingDownAndUp(device)
}

internal fun MacrobenchmarkScope.bringAWholeDayIntoView() {
    findWholeDayScrollingToIt()
}

internal fun MacrobenchmarkScope.openFirstDay() {
    findWholeDayScrollingToIt().click()
    device.findWhenShown(By.res(DAY_CONTENT_TAG).hasDescendant(By.res(DAY_PASSAGES_TAG)))
}

internal fun MacrobenchmarkScope.scrollDay() {
    device.findWhenShown(By.res(DAY_CONTENT_TAG)).flingDownAndUp(device)
}

internal fun MacrobenchmarkScope.openFirstChapter() {
    val passages = device.findWhenShown(By.res(DAY_PASSAGES_TAG))
    // Why: each chapter row is a clickable holding its own read checkbox, also clickable. The
    // widest clickable is the row, and tapping its center opens the chapter without marking it read.
    passages
        .findObjects(By.clickable(true))
        .maxBy { clickable -> clickable.visibleBounds.width() }
        .click()
    device.findWhenShown(By.res(READ_CHAPTERS_TAG))
}

internal fun MacrobenchmarkScope.scrollChapter() {
    device.findWhenShown(By.res(READ_CHAPTERS_TAG)).flingDownAndUp(device)
}

internal fun MacrobenchmarkScope.backToPlans() {
    repeat(MAX_BACK_PRESSES) {
        if (device.hasObject(By.res(PLANS_LIST_TAG))) return
        device.pressBack()
        device.waitForIdle()
    }
    device.findWhenShown(By.res(PLANS_LIST_TAG))
}

internal fun MacrobenchmarkScope.openBooks() {
    findTabRevealingBottomBar(BOOKS_TAB_TAG).click()
    device.findWhenShown(By.res(BOOKS_CONTENT_TAG).hasDescendant(By.clickable(true)))
}

internal fun MacrobenchmarkScope.scrollBooks() {
    device.findWhenShown(By.res(BOOKS_CONTENT_TAG)).flingDownAndUp(device)
}

// Why: the journeys go through the bottom bar and the one-pane day screen, which only the narrow
// layout draws. A tablet, an unfolded foldable or a phone on its side gets a navigation rail and a
// two-pane day, where no day row would ever sit above the bar.
private fun MacrobenchmarkScope.checkNarrowLayout() {
    val plansTab = findTabRevealingBottomBar(PLANS_TAB_TAG)
    check(plansTab.visibleBounds.top > device.displayHeight * BOTTOM_BAR_MIN_TOP_RATIO) {
        "The journeys need the narrow layout, with the tabs in a bottom bar: run them on a phone in portrait"
    }
}

private fun MacrobenchmarkScope.findWholeDayScrollingToIt(): UiObject2 {
    val list = device.findWhenShown(By.res(PLANS_LIST_TAG))
    repeat(MAX_SCROLLS_TO_A_DAY) {
        findWholeDayOnScreen(list)?.let { day -> return day }
        list.scroll(Direction.DOWN, SCROLL_TO_A_DAY_PERCENT)
        device.waitForIdle()
    }
    return findWholeDayOnScreen(list) ?: error("No day row of the plan was drawn whole above the bottom bar")
}

// Why: the list runs edge to edge under the bottom bar, so a row the list draws whole can still
// sit behind the bar, and a tap on it would land on a tab. A row clipped at the top of the list
// would take the tap somewhere else too. Once the list scrolled down the bar has exited, and the
// list's own bottom is the limit.
private fun MacrobenchmarkScope.findWholeDayOnScreen(list: UiObject2): UiObject2? {
    val listBounds = list.visibleBounds
    val bottom = device.findObject(By.res(PLANS_TAB_TAG))?.visibleBounds?.top ?: listBounds.bottom
    return list
        .findObjects(By.res(PLAN_DAY_TAG))
        .firstOrNull { row ->
            row.visibleBounds.isWhollyBetween(
                top = listBounds.top,
                bottom = bottom,
            )
        }
}

// Why: the bottom bar exits when the plans list scrolls down and only comes back when it scrolls
// up, so after a journey scrolled the list it may still be off screen.
private fun MacrobenchmarkScope.findTabRevealingBottomBar(tabTag: String): UiObject2 {
    repeat(MAX_SCROLLS_TO_REVEAL_THE_BAR) {
        device.wait(Until.findObject(By.res(tabTag)), BAR_SETTLE_MILLIS)?.let { tab -> return tab }
        device.findWhenShown(By.res(PLANS_LIST_TAG)).scroll(Direction.UP, SCROLL_TO_REVEAL_THE_BAR_PERCENT)
        device.waitForIdle()
    }
    return device.findWhenShown(By.res(tabTag))
}

private fun Rect.isWhollyBetween(
    top: Int,
    bottom: Int,
): Boolean = this.top > top && this.bottom < bottom

// Why: flinging from the edges would start a back gesture or reach the bottom bar instead.
private fun UiObject2.flingDownAndUp(device: UiDevice) {
    setGestureMargin(device.displayWidth / 5)
    repeat(FLINGS) {
        fling(Direction.DOWN)
        device.waitForIdle()
    }
    repeat(FLINGS) {
        fling(Direction.UP)
        device.waitForIdle()
    }
}

private fun UiDevice.findWhenShown(selector: BySelector): UiObject2 =
    wait(Until.findObject(selector), TIMEOUT_MILLIS) ?: error("Nothing on screen matched $selector")
