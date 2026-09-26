package com.quare.bibleplanner.core.navigation.slot

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.quare.bibleplanner.core.model.route.MainNavRouteDestination
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSharedTransitionApi::class)
internal class RootMainTabEntriesTest {
    @Test
    fun `GIVEN the main tab entries WHEN registering them THEN every tab destination resolves to an entry`() {
        // Given
        val tabEntries = RootMainTabEntries(sharedTransitionScope = unusedStub<SharedTransitionScope>())
        val unresolved = mutableListOf<NavKey>()

        // When
        val provider = entryProvider<NavKey>(
            fallback = { key ->
                unresolved += key
                NavEntry(key) {}
            },
        ) {
            tabEntries.register(
                scope = this,
                navigationBar = {},
                navigationRail = {},
                animatedContentScope = unusedStub<AnimatedContentScope>(),
            )
        }

        listOf(
            MainNavRouteDestination.Plans,
            MainNavRouteDestination.Books,
            MainNavRouteDestination.Profile,
        ).forEach(provider::invoke)

        // Then
        assertEquals(emptyList(), unresolved)
    }

    private inline fun <reified T : Any> unusedStub(): T = Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java),
    ) { _, method, _ -> error("${method.name} is only reached when an entry is drawn") } as T
}
