package com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl

import com.quare.bibleplanner.core.provider.analytics.domain.service.AnalyticsService
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.ObserveTesterUserProperty
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.ObserveStringRemoteConfig
import com.quare.bibleplanner.core.user.domain.usecase.ObserveAuthenticatedUserId
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal class ObserveTesterUserPropertyUseCase(
    private val observeAuthenticatedUserId: ObserveAuthenticatedUserId,
    private val observeStringRemoteConfig: ObserveStringRemoteConfig,
    private val analyticsService: AnalyticsService,
) : ObserveTesterUserProperty {
    override suspend fun invoke() {
        combine(
            observeAuthenticatedUserId(),
            observeStringRemoteConfig(
                key = TESTER_USER_IDS_KEY,
                default = EMPTY_JSON_ARRAY,
            ),
        ) { userId, allowlistJson ->
            userId != null && userId in allowlistJson.toTesterIds()
        }.distinctUntilChanged().collect { isTester ->
            analyticsService.setUserProperty(
                name = IS_TESTER_PROPERTY,
                value = isTester.toString(),
            )
        }
    }

    private fun String.toTesterIds(): List<String> =
        runCatching { Json.decodeFromString(ListSerializer(String.serializer()), this) }
            .getOrDefault(emptyList())

    companion object {
        private const val TESTER_USER_IDS_KEY = "tester_user_ids"
        private const val IS_TESTER_PROPERTY = "is_tester"
        private const val EMPTY_JSON_ARRAY = "[]"
    }
}
