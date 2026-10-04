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
    fun `finds a route by name or by its nested name`() {
        // When
        val names = listOf("readnavroute", "MainNavRouteDestination.Books", "Books").map { name ->
            catalog.find(name).name
        }

        // Then
        assertEquals(
            expected = listOf("ReadNavRoute", "MainNavRouteDestination.Books", "MainNavRouteDestination.Books"),
            actual = names,
        )
    }

    @Test
    fun `describes the arguments of a route`() {
        // When
        val signatures = listOf("ReadNavRoute", "ChatNavRoute", "MainNavRoute").map { name ->
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
    fun `creates a route from its JSON arguments`() {
        // When
        val routes = listOf(
            catalog.create(
                route = catalog.find("ChatNavRoute"),
                arguments = Json
                    .parseToJsonElement(
                        """{"source": "CHAPTER_STUDY", "dayNumber": null, "weekNumber": null, "readingPlanType": null,
                        "bookId": "GEN", "chapterNumber": 3}""",
                    ).jsonObject,
            ),
            catalog.create(
                route = catalog.find("Plans"),
                arguments = Json.parseToJsonElement("{}").jsonObject,
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
    fun `names a route as the catalog lists it`() {
        // When
        val name = catalog.nameOf(
            ReadNavRoute(
                bookId = "JHN",
                chapterNumber = 3,
                isChapterRead = false,
                isFromBookDetails = false,
                targetVerseNumbers = emptyList(),
            ),
        )

        // Then
        assertEquals(
            expected = "ReadNavRoute",
            actual = name,
        )
    }

    @Test
    fun `an unknown route points to the routes command`() {
        // When
        val error = assertFailsWith<IllegalArgumentException> { catalog.find("Nowhere") }

        // Then
        assertEquals(
            expected = "unknown route Nowhere; run `routes` to list them",
            actual = error.message,
        )
    }
}
