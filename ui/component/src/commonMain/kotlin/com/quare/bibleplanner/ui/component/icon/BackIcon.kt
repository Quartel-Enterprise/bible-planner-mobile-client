package com.quare.bibleplanner.ui.component.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.back
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.core.provider.platform.getPlatform
import com.quare.bibleplanner.core.provider.platform.isApplePlatform
import org.jetbrains.compose.resources.stringResource

/**
 * A reusable back icon button composable.
 *
 * @param onBackClick Callback invoked when the back button is clicked
 * @param modifier Optional modifier to apply to the IconButton
 */
@Composable
fun BackIcon(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val platform = remember { getPlatform() }
    CommonIconButton(
        modifier = modifier,
        onClick = onBackClick,
        imageVector = platform.toArrowBackIcon(),
        contentDescription = stringResource(Res.string.back),
    )
}

private fun Platform.toArrowBackIcon(): ImageVector = if (isApplePlatform()) {
    Icons.AutoMirrored.Filled.ArrowBackIos
} else {
    Icons.AutoMirrored.Filled.ArrowBack
}
