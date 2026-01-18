package com.quare.bibleplanner.feature.login.presentation.component.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SocialLoginButton(
    modifier: Modifier = Modifier,
    textResource: StringResource,
    drawableResource: DrawableResource,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        val text = stringResource(textResource)
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(resource = drawableResource),
                contentDescription = text,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
