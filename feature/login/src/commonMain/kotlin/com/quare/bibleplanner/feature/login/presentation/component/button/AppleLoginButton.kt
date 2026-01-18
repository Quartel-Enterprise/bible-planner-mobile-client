package com.quare.bibleplanner.feature.login.presentation.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.apple_icon_dark
import bibleplanner.feature.login.generated.resources.apple_icon_white
import bibleplanner.feature.login.generated.resources.continue_with_apple
import com.quare.bibleplanner.ui.theme.isAppInDarkTheme

@Composable
internal fun AppleLoginButton(onClick: () -> Unit) {
    SocialLoginButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        textResource = Res.string.continue_with_apple,
        drawableResource = if (isAppInDarkTheme()) {
            Res.drawable.apple_icon_white
        } else {
            Res.drawable.apple_icon_dark
        },
    )
}
