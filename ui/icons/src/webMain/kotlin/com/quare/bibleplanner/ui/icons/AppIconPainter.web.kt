package com.quare.bibleplanner.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
actual fun rememberAppIconPainter(icon: AppIcon): Painter = rememberVectorPainter(icon.material())
