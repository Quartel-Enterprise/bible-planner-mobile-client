package com.quare.bibleplanner.feature.verse.share.presentation.mapper

import bibleplanner.feature.verse.share.generated.resources.Res
import bibleplanner.feature.verse.share.generated.resources.share_image_save_failed
import bibleplanner.feature.verse.share.generated.resources.share_image_saved
import com.quare.bibleplanner.feature.verse.share.presentation.model.ImageSaveResult
import org.jetbrains.compose.resources.StringResource

internal fun ImageSaveResult.toMessage(): StringResource? = when (this) {
    ImageSaveResult.SAVED -> Res.string.share_image_saved
    ImageSaveResult.FAILED -> Res.string.share_image_save_failed
    ImageSaveResult.CANCELLED -> null
}
