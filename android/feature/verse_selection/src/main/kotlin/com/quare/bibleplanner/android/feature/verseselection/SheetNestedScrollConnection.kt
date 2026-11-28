package com.quare.bibleplanner.android.feature.verseselection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/*
 * Why: the panel scrolls inside the sheet and would otherwise take every vertical drag, leaving drag to dismiss on the
 * handle only. Like ModalBottomSheet, a drag the content can't use moves the sheet, and a sheet already pulled down
 * takes the drag back up before the content scrolls.
 */
internal class SheetNestedScrollConnection(
    private val sheetOffset: () -> Float,
    private val moveSheetBy: (Float) -> Float,
    private val settle: suspend (velocity: Float) -> Unit,
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset = if (available.y < 0f && source == NestedScrollSource.UserInput && sheetOffset() > 0f) {
        Offset(
            x = 0f,
            y = moveSheetBy(available.y),
        )
    } else {
        Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = if (available.y > 0f && source == NestedScrollSource.UserInput) {
        Offset(
            x = 0f,
            y = moveSheetBy(available.y),
        )
    } else {
        Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity = if (sheetOffset() > 0f) {
        settle(available.y)
        available
    } else {
        Velocity.Zero
    }

    // Why: only a sheet already pulled down settles; a fling that just reaches the content's top must not dismiss.
    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity = if (sheetOffset() > 0f) {
        settle(available.y)
        available
    } else {
        Velocity.Zero
    }
}
