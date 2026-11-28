package com.quare.bibleplanner.core.provider.billing.data.datasource

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

internal actual fun createBillingHttpEngine(): HttpClientEngine = Js.create()
