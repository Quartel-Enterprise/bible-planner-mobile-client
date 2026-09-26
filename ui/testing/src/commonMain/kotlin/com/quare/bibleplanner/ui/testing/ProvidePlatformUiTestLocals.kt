package com.quare.bibleplanner.ui.testing

import androidx.compose.runtime.Composable

@Composable
internal expect fun ProvidePlatformUiTestLocals(content: @Composable () -> Unit)
