package com.quare.bibleplanner.feature.editprofile.presentation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.quare.bibleplanner.core.utils.suspendRunCatching
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DecodeImageBitmapImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : DecodeImageBitmap {
    override suspend fun invoke(file: PlatformFile): Result<ImageBitmap> = suspendRunCatching {
        withContext(dispatcher) { file.readBytes().decodeToImageBitmap() }
    }
}
