package com.quare.bibleplanner.feature.accountdetails.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.AccountDetailsNavRoute
import com.quare.bibleplanner.core.model.route.getSheetPane

fun EntryProviderScope<NavKey>.accountDetails() {
    entry<AccountDetailsNavRoute>(metadata = getSheetPane()) {
        AccountDetailsSheet()
    }
}
