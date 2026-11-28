@file:JsExport

package com.quare.bibleplanner.bridge

import com.quare.bibleplanner.di.initializeKoinForWeb

/*
 * Why: the React app calls this once before it creates any presenter, the JS analogue of the iOS App init that
 * starts Koin. A second call is ignored.
 */
fun startBiblePlanner() {
    initializeKoinForWeb()
}
