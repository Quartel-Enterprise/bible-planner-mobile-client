package com.quare.bibleplanner.core.image

import androidx.compose.ui.graphics.ImageBitmap
import io.github.vinceglb.filekit.PlatformFile

fun interface DecodeImageBitmap {
    suspend operator fun invoke(file: PlatformFile): Result<ImageBitmap>
}
