package com.quare.bibleplanner.feature.bookdetails.presentation.component

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import bibleplanner.ui.component.generated.resources.back
import com.quare.bibleplanner.core.books.presentation.model.BookGroup
import com.quare.bibleplanner.core.books.util.toBookNameResource
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.provider.platform.Platform
import com.quare.bibleplanner.feature.bookdetails.fixture.bookDetailsUiState
import com.quare.bibleplanner.feature.bookdetails.presentation.model.BookDetailsUiState
import com.quare.bibleplanner.ui.testing.AnimationsDisabled
import com.quare.bibleplanner.ui.testing.SharedTransitionTestContent
import com.quare.bibleplanner.ui.testing.setUiTestContent
import com.quare.bibleplanner.ui.utils.SharedTransitionModifierFactory
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertTrue
import bibleplanner.ui.component.generated.resources.Res as ComponentRes

@OptIn(ExperimentalTestApi::class)
internal class BookDetailsTopBarUiTest {
    private var isBookDetailsShown by mutableStateOf(false)

    @Test
    fun `GIVEN animations disabled WHEN opening a book THEN shows its name in the top bar`() =
        runComposeUiTest(effectContext = AnimationsDisabled) {
            // Given
            val bookName = getString(BookId.PSA.toBookNameResource())
            prepareScenario(
                uiState = bookDetailsUiState(bookCategoryName = getString(BookGroup.WisdomBooks.titleRes)),
                bookName = bookName,
            )

            // When
            isBookDetailsShown = true
            waitForIdle()

            // Then
            val bookNameNode = onNodeWithText(bookName)
            bookNameNode.assertIsDisplayed()
            val bookNameBounds = bookNameNode.getUnclippedBoundsInRoot()
            val backBounds = onNodeWithContentDescription(getString(ComponentRes.string.back))
                .getUnclippedBoundsInRoot()
            assertTrue(
                actual = bookNameBounds.top < backBounds.bottom && bookNameBounds.bottom > backBounds.top,
                message = "Book name at $bookNameBounds should sit in the back button row at $backBounds",
            )
        }

    private fun ComposeUiTest.prepareScenario(
        uiState: BookDetailsUiState,
        bookName: String,
    ) {
        setUiTestContent {
            SharedTransitionTestContent(
                isTargetShown = isBookDetailsShown,
                source = { animatedContentScope ->
                    Text(
                        text = bookName,
                        modifier = SharedTransitionModifierFactory.getBookNameSharedTransitionModifier(
                            sharedTransitionScope = this,
                            animatedVisibilityScope = animatedContentScope,
                            bookName = bookName,
                        ),
                    )
                },
                target = { animatedContentScope ->
                    BookDetailsTopBar(
                        platform = Platform.Android,
                        state = uiState,
                        isScrolled = false,
                        sharedTransitionScope = this,
                        animatedVisibilityScope = animatedContentScope,
                        onEvent = {},
                    )
                },
            )
        }
    }
}
