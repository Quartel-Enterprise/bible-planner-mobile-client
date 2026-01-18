package com.quare.bibleplanner.feature.login.presentation.component.button

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.continue_with_google
import bibleplanner.feature.login.generated.resources.google_icon_dark
import bibleplanner.feature.login.generated.resources.google_icon_white
import com.quare.bibleplanner.ui.theme.isAppInDarkTheme

@Composable
fun GoogleLoginButton(onClick: () -> Unit) {
    SocialLoginButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        textResource = Res.string.continue_with_google,
        drawableResource = if (isAppInDarkTheme()) {
            Res.drawable.google_icon_white
        } else {
            Res.drawable.google_icon_dark
        },
    )
}
