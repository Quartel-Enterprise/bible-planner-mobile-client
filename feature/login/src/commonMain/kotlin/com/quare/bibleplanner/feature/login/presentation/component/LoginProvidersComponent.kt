package com.quare.bibleplanner.feature.login.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bibleplanner.feature.login.generated.resources.Res
import bibleplanner.feature.login.generated.resources.apple_icon_dark
import bibleplanner.feature.login.generated.resources.apple_icon_white
import bibleplanner.feature.login.generated.resources.continue_with_apple
import bibleplanner.feature.login.generated.resources.continue_with_google
import bibleplanner.feature.login.generated.resources.google_icon_dark
import bibleplanner.feature.login.generated.resources.google_icon_white
import com.quare.bibleplanner.core.utils.isLastIndex
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.component.button.SocialLoginButton
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer
import com.quare.bibleplanner.ui.theme.isAppInDarkTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun LoginProvidersComponent(
    providers: List<LoginProvider>,
    onProviderClick: (LoginProvider) -> Unit,
    loadingProvider: LoginProvider?,
) {
    providers.forEachIndexed { index, provider ->
        SocialLoginButton(
            modifier = Modifier.fillMaxWidth(),
            textResource = provider.textResource,
            drawableResource = provider.iconResource(),
            onClick = { onProviderClick(provider) },
            isLoading = loadingProvider == provider,
        )
        if (!providers.isLastIndex(index)) {
            VerticalSpacer()
        }
    }
}

private val LoginProvider.textResource: StringResource
    get() = when (this) {
        LoginProvider.GOOGLE -> Res.string.continue_with_google
        LoginProvider.APPLE -> Res.string.continue_with_apple
    }

@Composable
private fun LoginProvider.iconResource(): DrawableResource {
    val isDark = isAppInDarkTheme()
    return when (this) {
        LoginProvider.GOOGLE -> if (isDark) Res.drawable.google_icon_white else Res.drawable.google_icon_dark
        LoginProvider.APPLE -> if (isDark) Res.drawable.apple_icon_white else Res.drawable.apple_icon_dark
    }
}
