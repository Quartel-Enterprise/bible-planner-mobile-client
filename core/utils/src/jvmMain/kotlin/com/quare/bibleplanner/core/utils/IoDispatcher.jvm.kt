package com.quare.bibleplanner.core.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
