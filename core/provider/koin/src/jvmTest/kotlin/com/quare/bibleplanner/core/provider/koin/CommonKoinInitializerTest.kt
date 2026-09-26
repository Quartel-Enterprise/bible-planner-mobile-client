package com.quare.bibleplanner.core.provider.koin

import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommonKoinInitializerTest {
    private val platformModule = module {
        single(named(PLATFORM_QUALIFIER)) { PLATFORM_NAME }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `GIVEN platform modules WHEN starting koin THEN loads them alongside the common modules`() {
        // When
        commonKoinInitializer(platformModules = listOf(platformModule))

        // Then
        assertEquals(PLATFORM_NAME, GlobalContext.get().get<String>(named(PLATFORM_QUALIFIER)))
    }

    @Test
    fun `GIVEN an app config WHEN starting koin THEN applies it to the koin application`() {
        // When
        commonKoinInitializer(
            platformModules = listOf(platformModule),
            config = { properties(mapOf(PROPERTY_KEY to PROPERTY_VALUE)) },
        )

        // Then
        assertEquals(PROPERTY_VALUE, GlobalContext.get().getProperty<String>(PROPERTY_KEY))
    }

    private companion object {
        const val PLATFORM_QUALIFIER = "platform"
        const val PLATFORM_NAME = "desktop"
        const val PROPERTY_KEY = "environment"
        const val PROPERTY_VALUE = "test"
    }
}
