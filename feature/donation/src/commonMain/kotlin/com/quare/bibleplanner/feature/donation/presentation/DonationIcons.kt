package com.quare.bibleplanner.feature.donation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object DonationIcons {
    val OnChain: ImageVector
        @Composable
        get() = ImageVector
            .Builder(
                name = "OnChain",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(21f, 16.5f)
                curveTo(21f, 16.88f, 20.79f, 17.21f, 20.47f, 17.38f)
                lineTo(12.57f, 21.82f)
                curveTo(12.41f, 21.94f, 12.21f, 22f, 12f, 22f)
                curveTo(11.79f, 22f, 11.59f, 21.94f, 11.43f, 21.82f)
                lineTo(3.53f, 17.38f)
                curveTo(3.21f, 17.21f, 3f, 16.88f, 3f, 16.5f)
                verticalLineTo(7.5f)
                curveTo(3f, 7.12f, 3.21f, 6.79f, 3.53f, 6.62f)
                lineTo(11.43f, 2.18f)
                curveTo(11.59f, 2.06f, 11.79f, 2f, 12f, 2f)
                curveTo(12.21f, 2f, 12.41f, 2.06f, 12.57f, 2.18f)
                lineTo(20.47f, 6.62f)
                curveTo(20.79f, 6.79f, 21f, 7.12f, 21f, 7.5f)
                verticalLineTo(16.5f)
                close()
                moveTo(12f, 4.15f)
                lineTo(6.04f, 7.5f)
                lineTo(12f, 10.85f)
                lineTo(17.96f, 7.5f)
                lineTo(12f, 4.15f)
                close()
                moveTo(5f, 15.91f)
                lineTo(11f, 19.29f)
                verticalLineTo(12.58f)
                lineTo(5f, 9.21f)
                verticalLineTo(15.91f)
                close()
                moveTo(19f, 15.91f)
                verticalLineTo(9.21f)
                lineTo(13f, 12.58f)
                verticalLineTo(19.29f)
                lineTo(19f, 15.91f)
                close()
            }.build()

    val LightningBolt: ImageVector
        @Composable
        get() = ImageVector
            .Builder(
                name = "LightningBolt",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(7f, 2f)
                verticalLineTo(13f)
                horizontalLineTo(10f)
                verticalLineTo(22f)
                lineTo(17f, 10f)
                horizontalLineTo(13f)
                lineTo(17f, 2f)
                horizontalLineTo(7f)
                close()
            }.build()

    val Tron: ImageVector
        @Composable
        get() = ImageVector
            .Builder(
                name = "Tron",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2f)
                lineTo(4.5f, 20.29f)
                lineTo(5.21f, 21f)
                lineTo(12f, 18f)
                lineTo(18.79f, 21f)
                lineTo(19.5f, 20.29f)
                lineTo(12f, 2f)
                close()
            }.build()

    val Ethereum: ImageVector
        @Composable
        get() = ImageVector
            .Builder(
                name = "Ethereum",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            ).path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2f)
                lineTo(4.5f, 14.5f)
                lineTo(12f, 18f)
                lineTo(19.5f, 14.5f)
                lineTo(12f, 2f)
                close()
                moveTo(12f, 22f)
                lineTo(4.5f, 15.5f)
                lineTo(12f, 19f)
                lineTo(19.5f, 15.5f)
                lineTo(12f, 22f)
                close()
            }.build()
}
