package com.quare.bibleplanner.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setUiTestContent(content: @Composable () -> Unit) {
    setContent {
        PlatformUiTestContent(content = content)
    }
}
