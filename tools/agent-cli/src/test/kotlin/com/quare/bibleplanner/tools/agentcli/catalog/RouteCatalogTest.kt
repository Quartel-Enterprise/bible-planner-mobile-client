package com.quare.bibleplanner.tools.agentcli.catalog

import com.quare.bibleplanner.core.model.route.ChatEntrySource
import com.quare.bibleplanner.core.model.route.ChatNavRoute
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import com.quare.bibleplanner.core.model.route.ReadNavRoute
import com.quare.bibleplanner.core.model.route.navigationSavedStateConfiguration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RouteCatalogTest {
    private val catalog = RouteCatalog(
        serializersModule = navigationSavedStateConfiguration.serializersModule,
        json = Json { serializersModule = navigationSavedStateConfiguration.serializersModule },
    )

    @Test
    fun `GIVEN the route catalog WHEN finding routes by full or nested name THEN finds them ignoring case`() {
        // Given
        val queries = listOf("readnavroute", "MainNavRouteDestination.Books", "Books")

        // When
        val names = queries.map { name ->
            catalog.find(name).name
        }

        // Then
        assertEquals(
            expected = listOf("ReadNavRoute", "MainNavRouteDestination.Books", "MainNavRouteDestination.Books"),
            actual = names,
        )
    }

    @Test
    fun `GIVEN routes with and without arguments WHEN describing them THEN lists each argument with its type`() {
        // Given
        val routeNames = listOf("ReadNavRoute", "ChatNavRoute", "MainNavRoute")

        // When
        val signatures = routeNames.map { name ->
            catalog.getSignature(catalog.find(name))
        }

        // Then
        assertEquals(
            expected = listOf(
                "(bookId: String, chapterNumber: Int, isChapterRead: Boolean, isFromBookDetails: Boolean, " +
                    "targetVerseNumbers: ArrayList<Int>)",
                "(source: ChatEntrySource, dayNumber: Int?, weekNumber: Int?, readingPlanType: String?, " +
                    "bookId: String?, chapterNumber: Int?)",
                "",
            ),
            actual = signatures,
        )
    }

    @Test
    fun `GIVEN JSON arguments WHEN creating routes THEN builds the routes they describe`() {
        // Given
        val chatArguments = Json
            .parseToJsonElement(
                """{"source": "CHAPTER_STUDY", "dayNumber": null, "weekNumber": null, "readingPlanType": null,
                "bookId": "GEN", "chapterNumber": 3}""",
            ).jsonObject
        val plansArguments = Json.parseToJsonElement("{}").jsonObject

        // When
        val routes = listOf(
            catalog.create(
                route = catalog.find("ChatNavRoute"),
                arguments = chatArguments,
            ),
            catalog.create(
                route = catalog.find("Plans"),
                arguments = plansArguments,
            ),
        )

        // Then
        assertEquals(
            expected = listOf(
                ChatNavRoute(
                    source = ChatEntrySource.CHAPTER_STUDY,
                    dayNumber = null,
                    weekNumber = null,
                    readingPlanType = null,
                    bookId = "GEN",
                    chapterNumber = 3,
                ),
                MainNavRouteDestination.Plans,
            ),
            actual = routes,
        )
    }

    @Test
    fun `GIVEN a route instance WHEN naming it THEN uses the name the catalog lists`() {
        // Given
        val route = ReadNavRoute(
            bookId = "JHN",
            chapterNumber = 3,
            isChapterRead = false,
            isFromBookDetails = false,
            targetVerseNumbers = emptyList(),
        )

        // When
        val name = catalog.nameOf(route)

        // Then
        assertEquals(
            expected = "ReadNavRoute",
            actual = name,
        )
    }

    @Test
    fun `GIVEN an unknown route WHEN finding it THEN points to the routes command`() {
        // Given
        val routeName = "Nowhere"

        // When
        val error = assertFailsWith<IllegalArgumentException> { catalog.find(routeName) }

        // Then
        assertEquals(
            expected = "unknown route Nowhere; run `routes` to list them",
            actual = error.message,
        )
    }
}
