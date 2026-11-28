package com.quare.bibleplanner.android.navigation

import java.util.ServiceLoader

/*
 * Why: R8 replaces load(X, X's class loader) followed directly by iterator() with plain constructor calls, so
 * release builds skip the reflective scan on the first frame; toList() on the loader would hide that call from it.
 */
fun loadNativeEntryFactories(): List<NativeEntryFactory> = ServiceLoader
    .load(NativeEntryFactory::class.java, NativeEntryFactory::class.java.classLoader)
    .iterator()
    .asSequence()
    .toList()
