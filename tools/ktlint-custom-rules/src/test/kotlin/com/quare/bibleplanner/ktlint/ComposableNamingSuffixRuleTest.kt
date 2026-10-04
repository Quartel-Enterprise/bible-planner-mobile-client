package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ComposableNamingSuffixRuleTest {
    private val composableNamingSuffixRuleAssertThat = assertThatRule { ComposableNamingSuffixRule() }

    @Test
    fun `GIVEN a composable whose name ends in a word outside the list WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            @Composable
            fun BookEntry(title: String) {
                Text(text = title)
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("BookEntry"))
    }

    @Test
    fun `GIVEN a private composable with a disallowed suffix WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            @Composable
            private fun PlayerControls() {
                Row {}
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("PlayerControls"))
    }

    @Test
    fun `GIVEN a composable with an allowed word anywhere but the end WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            @Composable
            fun ScreenWrapper() {
                Box {}
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("ScreenWrapper"))
    }

    @Test
    fun `GIVEN a composable whose suffix is not capitalized WHEN linting THEN reports it`() {
        // Given
        val code =
            """
            @Composable
            fun Bookscreen() {
                Box {}
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("Bookscreen"))
    }

    @Test
    fun `GIVEN composables that end in an allowed suffix WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Composable
            fun BooksScreen() {
                Box {}
            }

            @Composable
            private fun BookRow(title: String) {
                Text(text = title)
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a composable with the component fallback suffix WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Composable
            fun PlayerControlsComponent() {
                Row {}
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN lowercase composables that return a value WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Composable
            fun rememberDayProgress(): Book? = remember { null }

            @Composable
            fun chapterCountText(count: Int): String = count.toString()
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a preview named after what it previews WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @Preview
            @Composable
            private fun BookRowPreview() {
                BookRow(title = "Book")
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN a preview with a multi preview annotation WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            @PreviewLightDark
            @Composable
            private fun BookRowPreview() {
                BookRow(title = "Book")
            }
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }

    @Test
    fun `GIVEN an uppercase function that is not a composable WHEN linting THEN reports nothing`() {
        // Given
        val code =
            """
            fun BookItem(title: String): Book = Book(title = title)
            """.trimIndent()

        // When
        val linted = composableNamingSuffixRuleAssertThat(code)

        // Then
        linted.hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Composable '$name' must end with one of the allowed suffixes: Action, Badge, Banner, Bar, Box, Bubble, " +
        "Button, Card, Cell, Chip, Collector, Column, Component, Content, Dialog, Display, Drawer, Effect, Fab, " +
        "Field, Footer, Grid, Handle, Header, Heading, Hero, Icon, Image, Indicator, Item, Label, Layout, Line, " +
        "List, Menu, Message, Option, Overlay, Pane, Panel, Picker, Pill, Rail, Root, Row, Scaffold, Screen, " +
        "Section, Sheet, Sidebar, Skeleton, Slider, Snackbar, Spacer, Spinner, Surface, Switch, Text, Theme, Tile, " +
        "Title, Toggle. When none describes it, use 'Component'"
