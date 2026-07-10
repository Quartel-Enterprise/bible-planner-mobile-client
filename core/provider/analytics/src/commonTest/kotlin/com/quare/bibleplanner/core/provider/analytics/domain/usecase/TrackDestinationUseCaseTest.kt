package com.quare.bibleplanner.core.provider.analytics.domain.usecase

import androidx.navigation3.runtime.NavKey
import com.quare.bibleplanner.core.model.route.LogoutNavRoute
import com.quare.bibleplanner.core.provider.analytics.domain.mapper.NavRouteToDestinationMapper
import com.quare.bibleplanner.core.provider.analytics.domain.model.AnalyticsParams
import com.quare.bibleplanner.core.provider.analytics.domain.model.Destination
import com.quare.bibleplanner.core.provider.analytics.domain.model.DestinationType
import com.quare.bibleplanner.core.provider.analytics.domain.usecase.impl.TrackDestinationUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackDestinationUseCaseTest {
    private val trackedEvents = mutableListOf<Pair<String, Map<String, Any>>>()

    @Test
    fun `GIVEN a route the mapper resolves WHEN invoked THEN fires destination_view with name and type`() {
        // Given
        val useCase = createUseCase(
            mapper = NavRouteToDestinationMapper {
                Destination(
                    name = "book_details",
                    type = DestinationType.SCREEN,
                    params = mapOf(AnalyticsParams.BOOK_ID to "genesis"),
                )
            },
        )

        // When
        useCase(LogoutNavRoute)

        // Then
        assertEquals(
            listOf(
                "destination_view" to mapOf<String, Any>(
                    AnalyticsParams.BOOK_ID to "genesis",
                    AnalyticsParams.DESTINATION_NAME to "book_details",
                    AnalyticsParams.DESTINATION_TYPE to "screen",
                ),
            ),
            trackedEvents,
        )
    }

    @Test
    fun `GIVEN a dialog route WHEN invoked THEN destination_type is lowercased dialog`() {
        // Given
        val useCase = createUseCase(
            mapper = NavRouteToDestinationMapper {
                Destination(name = "logout", type = DestinationType.DIALOG, params = emptyMap())
            },
        )

        // When
        useCase(LogoutNavRoute)

        // Then
        assertEquals("dialog", trackedEvents.single().second[AnalyticsParams.DESTINATION_TYPE])
    }

    @Test
    fun `GIVEN the mapper resolves nothing WHEN invoked THEN fires nothing`() {
        // Given
        val useCase = createUseCase(mapper = NavRouteToDestinationMapper { null })

        // When
        useCase(LogoutNavRoute)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    @Test
    fun `GIVEN a NavKey that is not a NavRoute WHEN invoked THEN fires nothing`() {
        // Given
        val useCase = createUseCase(
            mapper = NavRouteToDestinationMapper {
                Destination(name = "should_not_be_called", type = DestinationType.SCREEN, params = emptyMap())
            },
        )

        // When
        useCase(NonRouteNavKey)

        // Then
        assertTrue(trackedEvents.isEmpty())
    }

    private fun createUseCase(mapper: NavRouteToDestinationMapper): TrackDestinationUseCase = TrackDestinationUseCase(
        mapper = mapper,
        trackEvent = TrackEvent { name, params -> trackedEvents += name to params },
    )

    private object NonRouteNavKey : NavKey
}
