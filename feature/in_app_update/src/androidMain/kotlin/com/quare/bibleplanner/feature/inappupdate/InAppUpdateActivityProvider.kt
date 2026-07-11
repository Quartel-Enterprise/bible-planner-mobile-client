package com.quare.bibleplanner.feature.inappupdate

import android.app.Activity
import java.lang.ref.WeakReference

class InAppUpdateActivityProvider {
    private var activityRef: WeakReference<Activity>? = null

    var activity: Activity?
        get() = activityRef?.get()
        set(value) {
            activityRef = value?.let(::WeakReference)
        }
}
