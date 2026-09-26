package com.quare.bibleplanner.feature.profile.presentation.factory

import com.quare.bibleplanner.feature.profile.presentation.model.ProfileIcon
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileMenuItemPresentationModel
import com.quare.bibleplanner.feature.profile.presentation.model.ProfileOptionItemType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ProfileMenuOptionsFactoryTest {
    private val menuOptions: List<ProfileMenuItemPresentationModel> = listOf(
        ProfileMenuOptionsFactory.pro,
        ProfileMenuOptionsFactory.theme,
        ProfileMenuOptionsFactory.appLanguage,
        ProfileMenuOptionsFactory.instagram,
        ProfileMenuOptionsFactory.webApp,
        ProfileMenuOptionsFactory.deleteProgress,
        ProfileMenuOptionsFactory.deleteAccount,
        ProfileMenuOptionsFactory.studySuggestion,
        ProfileMenuOptionsFactory.editStartDate,
        ProfileMenuOptionsFactory.releaseNotes,
        ProfileMenuOptionsFactory.bibleVersion,
        ProfileMenuOptionsFactory.contactSupport,
        ProfileMenuOptionsFactory.rateApp,
        ProfileMenuOptionsFactory.checkForUpdate,
    )

    @Test
    fun `GIVEN the menu options WHEN reading their types THEN every menu row type appears exactly once`() {
        // Given
        val rowsOutsideTheMenu = setOf(
            ProfileOptionItemType.PRIVACY_POLICY,
            ProfileOptionItemType.TERMS,
            ProfileOptionItemType.DONATE,
        )

        // When
        val types = menuOptions.map(ProfileMenuItemPresentationModel::type)

        // Then
        assertEquals(
            expected = ProfileOptionItemType.entries - rowsOutsideTheMenu,
            actual = types.sortedBy(ProfileOptionItemType::ordinal),
        )
    }

    @Test
    fun `GIVEN the menu options WHEN reading their icons THEN only instagram uses a brand drawable`() {
        // Given
        val options = menuOptions

        // When
        val drawableOptions = options.filter { option -> option.icon is ProfileIcon.DrawableResourceIcon }

        // Then
        assertEquals(
            expected = listOf(ProfileOptionItemType.INSTAGRAM),
            actual = drawableOptions.map(ProfileMenuItemPresentationModel::type),
        )
    }

    @Test
    fun `GIVEN the destructive options WHEN reading their subtitles THEN both explain their consequence`() {
        // Given
        val destructiveOptions = listOf(
            ProfileMenuOptionsFactory.deleteProgress,
            ProfileMenuOptionsFactory.deleteAccount,
        )

        // When
        val subtitles = destructiveOptions.map(ProfileMenuItemPresentationModel::subtitle)

        // Then
        assertEquals(
            expected = 2,
            actual = subtitles.filterNotNull().distinct().size,
        )
    }
}
