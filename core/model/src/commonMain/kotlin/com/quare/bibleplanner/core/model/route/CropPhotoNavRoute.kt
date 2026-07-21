package com.quare.bibleplanner.core.model.route

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable

@Serializable
data class CropPhotoNavRoute(
    val file: PlatformFile,
) : NavRoute
