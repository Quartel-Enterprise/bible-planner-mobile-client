package com.quare.bibleplanner.core.provider.platform

import com.quare.bibleplanner.core.utils.DesktopOs

internal actual fun getPlatform(): Platform = when (DesktopOs.detect()) {
    DesktopOs.MAC -> Platform.Desktop.MacOs
    DesktopOs.WINDOWS -> Platform.Desktop.Windows
    DesktopOs.LINUX -> Platform.Desktop.Linux
}
