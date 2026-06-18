package com.quare.bibleplanner.core.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ApplicationScope(
    delegate: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CoroutineScope by delegate
