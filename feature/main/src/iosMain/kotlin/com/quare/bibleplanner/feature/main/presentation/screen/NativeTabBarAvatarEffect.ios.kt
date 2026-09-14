package com.quare.bibleplanner.feature.main.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.navigation3.runtime.NavKey
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.quare.bibleplanner.core.profile.domain.model.AvatarSource
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationIosIcon
import com.quare.bibleplanner.feature.main.presentation.model.MainNavigationItemModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGRectInset
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageRenderingMode
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarItem
import kotlin.math.max

private const val AVATAR_SIZE_POINTS = 26.0
private const val SELECTED_RING_WIDTH_POINTS = 2.0
private const val AVATAR_DECODE_SIZE_PIXELS = 128

@Composable
internal actual fun NativeTabBarAvatarEffect(
    mainNavigationModels: List<MainNavigationItemModel<NavKey>>,
    tabBarItems: List<UIKitUITabBarItem>,
) {
    val viewController = LocalUIViewController.current
    val platformContext = LocalPlatformContext.current
    val selectedRingColor = MaterialTheme.colorScheme.primary
    LaunchedEffect(mainNavigationModels, tabBarItems, selectedRingColor) {
        withFrameNanos { }
        mainNavigationModels.forEachIndexed { index, model ->
            val icon = model.presentationModel.icon as? MainNavigationIcon.Profile ?: return@forEachIndexed
            val photo = loadAvatarPhoto(
                avatar = icon.avatar,
                platformContext = platformContext,
            )
            val tabBars = viewController.view.subviews.filterIsInstance<UITabBar>()
            tabBars.forEach { tabBar ->
                val item = tabBar.items?.getOrNull(index) as? UITabBarItem ?: return@forEach
                item.applyProfileImages(
                    photo = photo,
                    iosIcon = model.presentationModel.iosIcon,
                    selectedRingColor = selectedRingColor,
                )
            }
        }
    }
}

private fun UITabBarItem.applyProfileImages(
    photo: UIImage?,
    iosIcon: MainNavigationIosIcon,
    selectedRingColor: Color,
) {
    if (photo == null) {
        image = UIImage.systemImageNamed(iosIcon.symbolName)
        selectedImage = UIImage.systemImageNamed(iosIcon.selectedSymbolName)
    } else {
        image = photo.toTabBarAvatar(ringColor = null)
        selectedImage = photo.toTabBarAvatar(ringColor = selectedRingColor)
    }
}

private suspend fun loadAvatarPhoto(
    avatar: AvatarSource,
    platformContext: PlatformContext,
): UIImage? = when (avatar) {
    is AvatarSource.Pending -> avatar.bytes.toUIImage()

    is AvatarSource.Remote -> fetchRemotePhoto(
        url = avatar.url,
        platformContext = platformContext,
    )

    AvatarSource.None -> null
}

private suspend fun fetchRemotePhoto(
    url: String,
    platformContext: PlatformContext,
): UIImage? {
    val request = ImageRequest
        .Builder(platformContext)
        .data(url)
        .size(AVATAR_DECODE_SIZE_PIXELS)
        .build()
    val result = SingletonImageLoader.get(platformContext).execute(request) as? SuccessResult ?: return null
    val encoded = Image.makeFromBitmap(result.image.toBitmap()).encodeToData(EncodedImageFormat.PNG)
    return encoded?.bytes?.toUIImage()
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toUIImage(): UIImage? {
    if (isEmpty()) return null
    val data = usePinned { pinned ->
        NSData.dataWithBytes(
            bytes = pinned.addressOf(0),
            length = size.toULong(),
        )
    }
    return UIImage.imageWithData(data)
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.toTabBarAvatar(ringColor: Color?): UIImage {
    val bounds = CGRectMake(
        x = 0.0,
        y = 0.0,
        width = AVATAR_SIZE_POINTS,
        height = AVATAR_SIZE_POINTS,
    )
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(AVATAR_SIZE_POINTS, AVATAR_SIZE_POINTS))
    val avatar = renderer.imageWithActions { _ ->
        UIBezierPath.bezierPathWithOvalInRect(bounds).addClip()
        val (imageWidth, imageHeight) = size.useContents { width to height }
        val scale = max(AVATAR_SIZE_POINTS / imageWidth, AVATAR_SIZE_POINTS / imageHeight)
        drawInRect(
            CGRectMake(
                x = (AVATAR_SIZE_POINTS - imageWidth * scale) / 2,
                y = (AVATAR_SIZE_POINTS - imageHeight * scale) / 2,
                width = imageWidth * scale,
                height = imageHeight * scale,
            ),
        )
        if (ringColor != null) {
            ringColor.toUIColor().setStroke()
            val ringInset = SELECTED_RING_WIDTH_POINTS / 2
            UIBezierPath
                .bezierPathWithOvalInRect(CGRectInset(bounds, ringInset, ringInset))
                .apply { lineWidth = SELECTED_RING_WIDTH_POINTS }
                .stroke()
        }
    }
    return avatar.imageWithRenderingMode(UIImageRenderingMode.UIImageRenderingModeAlwaysOriginal)
}

private fun Color.toUIColor(): UIColor = UIColor.colorWithRed(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)
