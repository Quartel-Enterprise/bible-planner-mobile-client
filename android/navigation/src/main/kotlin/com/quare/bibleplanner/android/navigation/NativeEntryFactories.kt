package com.quare.bibleplanner.android.navigation

import java.util.ServiceLoader

/*
 * Why: R8 replaces load(X, X's class loader) with direct constructor calls only when iterator() is the load
 * result's sole use; Iterable.toList() would keep the reflective scan of the APK on the first frame.
 */
fun loadNativeEntryFactories(): List<NativeEntryFactory> = ServiceLoader
    .load(
        NativeEntryFactory::class.java,
        NativeEntryFactory::class.java.classLoader,
    ).iterator()
    .asSequence()
    .toList()
