package com.quare.bibleplanner.android.navigation

import java.util.ServiceLoader

// Why: R8 recognizes this call shape (the interface's own class loader) and keeps the listed services.
fun loadNativeEntryFactories(): List<NativeEntryFactory> = ServiceLoader
    .load(NativeEntryFactory::class.java, NativeEntryFactory::class.java.classLoader)
    .toList()
