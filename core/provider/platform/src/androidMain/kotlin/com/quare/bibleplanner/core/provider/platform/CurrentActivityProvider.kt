package com.quare.bibleplanner.core.provider.platform

import android.app.Activity
import java.lang.ref.WeakReference

class CurrentActivityProvider {
    private var activityRef: WeakReference<Activity>? = null

    var activity: Activity?
        get() = activityRef?.get()
        set(value) {
            activityRef = value?.let(::WeakReference)
        }
}
