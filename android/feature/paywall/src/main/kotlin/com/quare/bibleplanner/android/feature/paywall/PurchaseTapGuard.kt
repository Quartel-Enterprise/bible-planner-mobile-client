package com.quare.bibleplanner.android.feature.paywall

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect

/*
 * Why: the ViewModel marks a purchase or restore as running only after it checks the login, so a second tap in
 * between would start the billing flow twice or open the login warning twice.
 */
internal class PurchaseTapGuard {
    private var isPending by mutableStateOf(false)

    fun run(
        isPurchasing: Boolean,
        action: () -> Unit,
    ) {
        if (!isPending && !isPurchasing) {
            isPending = true
            action()
        }
    }

    fun release() {
        isPending = false
    }
}

@Composable
internal fun rememberPurchaseTapGuard(isPurchasing: Boolean): PurchaseTapGuard {
    val guard = remember { PurchaseTapGuard() }
    // Why: a finished or failed purchase ends with isPurchasing going back to false.
    LaunchedEffect(guard, isPurchasing) {
        if (!isPurchasing) guard.release()
    }
    /*
     * Why: a tap that never starts a purchase (the login warning opens instead) leaves the entry and comes back to
     * it, and Navigation 3 resumes the entry only once it is on top of the back stack again.
     */
    LifecycleResumeEffect(guard) {
        guard.release()
        onPauseOrDispose { }
    }
    return guard
}
