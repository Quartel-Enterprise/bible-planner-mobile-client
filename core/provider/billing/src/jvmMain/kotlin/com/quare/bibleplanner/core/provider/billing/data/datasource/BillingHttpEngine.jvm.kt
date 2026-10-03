package com.quare.bibleplanner.core.provider.billing.data.datasource

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

internal actual fun createBillingHttpEngine(): HttpClientEngine = CIO.create()
