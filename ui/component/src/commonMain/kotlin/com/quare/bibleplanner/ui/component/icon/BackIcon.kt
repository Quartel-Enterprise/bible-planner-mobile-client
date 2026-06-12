package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.back
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.isApple
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackIcon(
    platform: Platform,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CommonIconButton(
        modifier = modifier,
        onClick = onBackClick,
        imageVector = platform.toArrowBackIcon(),
        contentDescription = stringResource(Res.string.back),
    )
}

private fun Platform.toArrowBackIcon(): ImageVector = if (isApple()) {
    Icons.AutoMirrored.Filled.ArrowBackIos
} else {
    Icons.AutoMirrored.Filled.ArrowBack
}
