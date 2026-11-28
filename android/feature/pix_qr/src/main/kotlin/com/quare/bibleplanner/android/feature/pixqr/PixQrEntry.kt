package com.quare.bibleplanner.android.feature.pixqr

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.quare.bibleplanner.android.designsystem.theme.ExpressiveTheme
import com.quare.bibleplanner.core.model.route.PixQrNavRoute
import com.quare.bibleplanner.feature.donation.pixqr.presentation.PixQrViewModel
import org.koin.compose.viewmodel.koinViewModel

fun PixQrNavRoute.toNavEntry(): NavEntry<NavKey> = NavEntry(
    key = this,
    metadata = DialogSceneStrategy.dialog(),
) {
    val viewModel = koinViewModel<PixQrViewModel>()
    ExpressiveTheme {
        PixQrContent(onEvent = viewModel::onEvent)
    }
}
