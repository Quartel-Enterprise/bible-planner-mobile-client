package com.quare.bibleplanner.ui.component.text

import androidx.compose.runtime.Composable
import bibleplanner.ui.component.generated.resources.Res
import bibleplanner.ui.component.generated.resources.megabytes_size
import org.jetbrains.compose.resources.stringResource

private const val BYTES_IN_MEGABYTE = 1_000_000
private const val DECIMAL_PLACE = 10

@Composable
fun megabytesText(bytes: Long): String = stringResource(
    Res.string.megabytes_size,
    bytes / BYTES_IN_MEGABYTE,
    bytes % BYTES_IN_MEGABYTE * DECIMAL_PLACE / BYTES_IN_MEGABYTE,
)
