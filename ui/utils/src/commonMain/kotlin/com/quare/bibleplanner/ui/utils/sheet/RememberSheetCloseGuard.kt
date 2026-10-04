package com.quare.bibleplanner.ui.utils.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun rememberSheetCloseGuard(): SheetCloseGuard {
    val sheetExitAnimation = LocalSheetExitAnimation.current
    val sheetCloseGuard = remember(sheetExitAnimation) { SheetCloseGuard(sheetExitAnimation) }
    LaunchedEffect(sheetCloseGuard, sheetCloseGuard.hasPendingClose) {
        if (sheetCloseGuard.hasPendingClose) {
            sheetCloseGuard.expirePendingClose()
        }
    }
    return sheetCloseGuard
}
