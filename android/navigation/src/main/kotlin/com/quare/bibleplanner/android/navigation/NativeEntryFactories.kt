package com.quare.bibleplanner.android.navigation

import java.util.ServiceLoader

/*
 * Why: R8 only replaces ServiceLoader with direct constructor calls (no APK read on the main thread at cold start)
 * when load() takes the interface's own class loader and its result goes straight to iterator().
 */
fun loadNativeEntryFactories(): List<NativeEntryFactory> = ServiceLoader
    .load(NativeEntryFactory::class.java, NativeEntryFactory::class.java.classLoader)
    .iterator()
    .asSequence()
    .toList()
