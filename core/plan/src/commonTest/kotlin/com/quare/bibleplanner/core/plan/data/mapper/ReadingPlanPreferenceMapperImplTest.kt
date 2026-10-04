package com.quare.bibleplanner.core.plan.data.mapper

import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadingPlanPreferenceMapperImplTest {
    private val mapper = ReadingPlanPreferenceMapperImpl()

    @Test
    fun `GIVEN every plan type WHEN storing and reading it back THEN round-trips`() {
        // Given
        val planTypes = ReadingPlanType.entries

        // When
        val roundTripped = planTypes.map { mapper.mapPreferenceToModel(mapper.mapModelToPreference(it)) }

        // Then
        assertEquals(ReadingPlanType.entries, roundTripped)
    }

    @Test
    fun `GIVEN every plan type WHEN storing it THEN uses the legacy preference values`() {
        // Given
        val planTypes = ReadingPlanType.entries

        // When
        val preferences = planTypes.map(mapper::mapModelToPreference)

        // Then
        assertEquals(listOf("chronological", "books"), preferences)
    }

    @Test
    fun `GIVEN a missing or unknown preference WHEN reading it THEN defaults to the chronological plan`() {
        // Given
        val missingPreference: String? = null
        val unknownPreference = "yearly"

        // When
        val fromMissing = mapper.mapPreferenceToModel(missingPreference)
        val fromUnknown = mapper.mapPreferenceToModel(unknownPreference)

        // Then
        assertEquals(ReadingPlanType.CHRONOLOGICAL, fromMissing)
        assertEquals(ReadingPlanType.CHRONOLOGICAL, fromUnknown)
    }
}
