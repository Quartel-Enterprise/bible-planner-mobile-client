package com.quare.bibleplanner.core.provider.billing.data.datasource

import io.ktor.client.engine.HttpClientEngine

internal expect fun createBillingHttpEngine(): HttpClientEngine
