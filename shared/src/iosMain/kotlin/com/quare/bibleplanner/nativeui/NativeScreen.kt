package com.quare.bibleplanner.nativeui

import platform.UIKit.UIViewController

/*
 * Why: a SwiftUI screen owns its ViewModelHolder itself (docs/architecture/native-ui-migration.md), so Compose only
 * hosts its view controller and tells it when it goes away for good, which clears the holder.
 */
interface NativeScreen {
    val viewController: UIViewController

    fun dispose()
}
