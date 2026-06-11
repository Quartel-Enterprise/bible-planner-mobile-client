package com.quare.bibleplanner.feature.login.presentation.component

import androidx.compose.runtime.Composable
import com.quare.bibleplanner.core.utils.isLastIndex
import com.quare.bibleplanner.feature.login.domain.model.LoginProvider
import com.quare.bibleplanner.feature.login.presentation.component.button.AppleLoginButton
import com.quare.bibleplanner.feature.login.presentation.component.button.GoogleLoginButton
import com.quare.bibleplanner.ui.component.spacer.VerticalSpacer

@Composable
fun LoginProvidersComponent(
    providers: List<LoginProvider>,
    onLoginWithGoogleClick: () -> Unit,
    onLoginWithAppleClick: () -> Unit,
    isGoogleLoading: Boolean,
    isAppleLoading: Boolean,
) {
    providers.forEachIndexed { index, provider ->
        when (provider) {
            LoginProvider.GOOGLE -> GoogleLoginButton(
                onClick = onLoginWithGoogleClick,
                isLoading = isGoogleLoading,
            )

            LoginProvider.APPLE -> AppleLoginButton(
                onClick = onLoginWithAppleClick,
                isLoading = isAppleLoading,
            )
        }
        if (!providers.isLastIndex(index)) {
            VerticalSpacer()
        }
    }
}
