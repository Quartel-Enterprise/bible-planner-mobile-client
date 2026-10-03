package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun rememberCameraPicker(onResult: (PlatformFile?) -> Unit): () -> Unit = {}
