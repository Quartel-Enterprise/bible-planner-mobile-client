package com.quare.bibleplanner.android.feature.editprofile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Rotate90DegreesCw
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quare.bibleplanner.core.image.getCircleCoverScale
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiEvent
import com.quare.bibleplanner.feature.editprofile.presentation.model.CropPhotoUiState
import com.quare.bibleplanner.feature.editprofile.presentation.model.ImageResult

private const val CIRCLE_WIDTH_RATIO = 0.9f
private const val HALF = 2f
private const val NO_ICON_ROTATION = 0f
private const val VERTICAL_FLIP_ICON_ROTATION = 90f
private val screenBackground = Color(0xFF0B0B0F)
private val cropScrim = Color(0xFF0B0B0F).copy(alpha = 0.72f)
private val cropCircleBorder = Color.White.copy(alpha = 0.9f)
private val zoomIconColor = Color.White.copy(alpha = 0.7f)
private val hintColor = Color.White.copy(alpha = 0.55f)
private val outlinedBorderColor = Color.White.copy(alpha = 0.25f)
private val orientationChipBackground = Color.White.copy(alpha = 0.12f)
private val maxCircleDiameter = 360.dp
private val smallZoomIconSize = 20.dp
private val largeZoomIconSize = 26.dp
private val actionButtonHeight = 48.dp
private val actionButtonRadius = 14.dp
private val cropCircleBorderWidth = 2.dp
private val orientationChipIconSize = 20.dp

@Composable
internal fun CropPhotoContent(
    uiState: CropPhotoUiState,
    onEvent: (CropPhotoUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Why: the header comes first so a slow decode still shows a way out.
        CropHeader(onCancel = { onEvent(CropPhotoUiEvent.OnCancelClick) })
        val bitmap = (uiState.image as? ImageResult.Loaded)?.bitmap ?: return@Column
        CropAreaBox(
            bitmap = bitmap,
            uiState = uiState,
            onEvent = onEvent,
        )
        ZoomControlColumn(
            zoom = uiState.zoom,
            zoomRange = uiState.zoomRange,
            onZoomChange = { onEvent(CropPhotoUiEvent.OnZoomChanged(it)) },
        )
        OrientationControlRow(
            onFlipHorizontal = { onEvent(CropPhotoUiEvent.OnFlipHorizontalClick) },
            onFlipVertical = { onEvent(CropPhotoUiEvent.OnFlipVerticalClick) },
            onRotate = { onEvent(CropPhotoUiEvent.OnRotateClick) },
        )
        CropActionRow(
            onCancel = { onEvent(CropPhotoUiEvent.OnCancelClick) },
            onConfirm = { onEvent(CropPhotoUiEvent.OnConfirmClick) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CropHeader(onCancel: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(R.string.edit_profile_crop_title)) },
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.edit_profile_close),
                )
            }
        },
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
        ),
    )
}

@Composable
private fun ColumnScope.CropAreaBox(
    bitmap: ImageBitmap,
    uiState: CropPhotoUiState,
    onEvent: (CropPhotoUiEvent) -> Unit,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    currentOnEvent(
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
            currentOnEvent(
                CropPhotoUiEvent.OnViewportMeasured(
                    areaWidth = areaWidthPx,
                    areaHeight = areaHeightPx,
                    circleDiameter = circleDiameterPx,
                ),
            )
        }
        val baseScale = getCircleCoverScale(
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
                    scaleX = uiState.zoom * uiState.orientation.horizontalScale,
                    scaleY = uiState.zoom * uiState.orientation.verticalScale,
                    rotationZ = uiState.orientation.rotationDegrees.toFloat(),
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
}

@Composable
private fun ZoomControlColumn(
    zoom: Float,
    zoomRange: ClosedFloatingPointRange<Float>,
    onZoomChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier.padding(
            start = 24.dp,
            end = 24.dp,
            top = 18.dp,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Image,
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
                imageVector = Icons.Rounded.Image,
                contentDescription = null,
                modifier = Modifier.size(largeZoomIconSize),
                tint = zoomIconColor,
            )
        }
        Text(
            text = stringResource(R.string.edit_profile_crop_hint),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            color = hintColor,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OrientationControlRow(
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onRotate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 14.dp,
                bottom = 12.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(
            space = 10.dp,
            alignment = Alignment.CenterHorizontally,
        ),
    ) {
        OrientationChip(
            icon = Icons.Rounded.Flip,
            iconRotation = NO_ICON_ROTATION,
            label = stringResource(R.string.edit_profile_crop_flip_horizontal),
            onClick = onFlipHorizontal,
        )
        OrientationChip(
            icon = Icons.Rounded.Flip,
            iconRotation = VERTICAL_FLIP_ICON_ROTATION,
            label = stringResource(R.string.edit_profile_crop_flip_vertical),
            onClick = onFlipVertical,
        )
        OrientationChip(
            icon = Icons.Rounded.Rotate90DegreesCw,
            iconRotation = NO_ICON_ROTATION,
            label = stringResource(R.string.edit_profile_crop_rotate),
            onClick = onRotate,
        )
    }
}

@Composable
private fun OrientationChip(
    icon: ImageVector,
    iconRotation: Float,
    label: String,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(orientationChipIconSize)
                    .graphicsLayer(rotationZ = iconRotation),
            )
        },
        shape = CircleShape,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = orientationChipBackground,
            labelColor = Color.White,
            leadingIconContentColor = Color.White,
        ),
        border = null,
    )
}

@Composable
private fun CropActionRow(
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
            Text(text = stringResource(R.string.edit_profile_cancel))
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(actionButtonHeight),
            shape = RoundedCornerShape(actionButtonRadius),
        ) {
            Text(text = stringResource(R.string.edit_profile_crop_confirm))
        }
    }
}
