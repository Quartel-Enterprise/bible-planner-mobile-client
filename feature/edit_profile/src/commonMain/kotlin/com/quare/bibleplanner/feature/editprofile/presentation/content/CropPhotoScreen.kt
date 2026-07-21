package com.quare.bibleplanner.feature.editprofile.presentation.content

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bibleplanner.feature.edit_profile.generated.resources.Res
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_cancel
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_close
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_crop_confirm
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_crop_hint
import bibleplanner.feature.edit_profile.generated.resources.edit_profile_crop_title
import com.quare.bibleplanner.core.image.circleCoverScale
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiState
import com.quare.bibleplanner.feature.editprofile.presentation.model.ImageResult
import org.jetbrains.compose.resources.stringResource

private const val CIRCLE_WIDTH_RATIO = 0.9f
private const val HALF = 2f

private val screenBackground = Color(0xFF0B0B0F)
private val cropScrim = Color(0xFF0B0B0F).copy(alpha = 0.72f)
private val cropCircleBorder = Color.White.copy(alpha = 0.9f)
private val zoomIconColor = Color.White.copy(alpha = 0.7f)
private val hintColor = Color.White.copy(alpha = 0.55f)
private val outlinedBorderColor = Color.White.copy(alpha = 0.25f)

private val headerHeight = 56.dp
private val headerButtonSize = 44.dp
private val maxCircleDiameter = 360.dp
private val smallZoomIconSize = 20.dp
private val largeZoomIconSize = 26.dp
private val actionButtonHeight = 48.dp
private val actionButtonRadius = 14.dp
private val cropCircleBorderWidth = 2.dp

@Composable
internal fun CropPhotoScreen(
    uiState: CropPhotoUiState,
    onEvent: (CropPhotoUiEvent) -> Unit,
) {
    val bitmap = (uiState.image as? ImageResult.Loaded)?.bitmap ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        CropHeader(onCancel = { onEvent(CropPhotoUiEvent.OnCancelClick) })

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        onEvent(
                            CropPhotoUiEvent.OnTransform(
                                panX = pan.x,
                                panY = pan.y,
                                zoomChange = gestureZoom,
                            ),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            val areaWidthPx = constraints.maxWidth.toFloat()
            val areaHeightPx = constraints.maxHeight.toFloat()
            val circleDiameterPx = minOf(
                areaWidthPx * CIRCLE_WIDTH_RATIO,
                areaHeightPx,
                with(density) { maxCircleDiameter.toPx() },
            )
            LaunchedEffect(areaWidthPx, areaHeightPx, circleDiameterPx) {
                onEvent(
                    CropPhotoUiEvent.OnViewportMeasured(
                        areaWidth = areaWidthPx,
                        areaHeight = areaHeightPx,
                        circleDiameter = circleDiameterPx,
                    ),
                )
            }

            val baseScale = circleCoverScale(
                imageWidth = bitmap.width,
                imageHeight = bitmap.height,
                circleDiameter = circleDiameterPx,
            )
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .requiredSize(
                        width = with(density) { (bitmap.width * baseScale).toDp() },
                        height = with(density) { (bitmap.height * baseScale).toDp() },
                    ).graphicsLayer(
                        scaleX = uiState.zoom,
                        scaleY = uiState.zoom,
                        translationX = uiState.offsetX,
                        translationY = uiState.offsetY,
                    ),
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
            ) {
                val radius = circleDiameterPx / HALF
                drawRect(color = cropScrim)
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    blendMode = BlendMode.Clear,
                )
                drawCircle(
                    color = cropCircleBorder,
                    radius = radius,
                    style = Stroke(width = cropCircleBorderWidth.toPx()),
                )
            }
        }

        ZoomControls(
            zoom = uiState.zoom,
            zoomRange = uiState.zoomRange,
            onZoomChange = { onEvent(CropPhotoUiEvent.OnZoomChanged(it)) },
        )

        CropActions(
            onCancel = { onEvent(CropPhotoUiEvent.OnCancelClick) },
            onConfirm = { onEvent(CropPhotoUiEvent.OnConfirmClick) },
        )
    }
}

@Composable
private fun CropHeader(onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(headerButtonSize),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.edit_profile_close),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(Res.string.edit_profile_crop_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Box(modifier = Modifier.size(headerButtonSize))
    }
}

@Composable
private fun ZoomControls(
    zoom: Float,
    zoomRange: ClosedFloatingPointRange<Float>,
    onZoomChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.padding(
            start = 24.dp,
            end = 24.dp,
            top = 18.dp,
            bottom = 12.dp,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(smallZoomIconSize),
                tint = zoomIconColor,
            )
            Slider(
                value = zoom,
                onValueChange = onZoomChange,
                modifier = Modifier.weight(1f),
                valueRange = zoomRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                ),
            )
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(largeZoomIconSize),
                tint = zoomIconColor,
            )
        }
        Text(
            text = stringResource(Res.string.edit_profile_crop_hint),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            color = hintColor,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CropActions(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 8.dp,
            bottom = 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(actionButtonHeight),
            shape = RoundedCornerShape(actionButtonRadius),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(
                width = 1.dp,
                color = outlinedBorderColor,
            ),
        ) {
            Text(text = stringResource(Res.string.edit_profile_cancel))
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(actionButtonHeight),
            shape = RoundedCornerShape(actionButtonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = stringResource(Res.string.edit_profile_crop_confirm))
        }
    }
}
