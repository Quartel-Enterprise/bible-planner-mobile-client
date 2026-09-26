package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.test.KtLintAssertThat.Companion.assertThatRule
import org.junit.jupiter.api.Test

class ComposableNamingSuffixRuleTest {
    private val composableNamingSuffixRuleAssertThat = assertThatRule { ComposableNamingSuffixRule() }

    @Test
    fun `flags a composable whose name ends in a word outside the list`() {
        val code =
            """
            @Composable
            fun BookEntry(title: String) {
                Text(text = title)
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("BookEntry"))
    }

    @Test
    fun `flags a private composable`() {
        val code =
            """
            @Composable
            private fun PlayerControls() {
                Row {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 13, buildViolationMessage("PlayerControls"))
    }

    @Test
    fun `flags a composable that carries an allowed word anywhere but the end`() {
        val code =
            """
            @Composable
            fun ScreenWrapper() {
                Box {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("ScreenWrapper"))
    }

    @Test
    fun `flags a composable whose suffix is not capitalized`() {
        val code =
            """
            @Composable
            fun Bookscreen() {
                Box {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code)
            .hasLintViolationWithoutAutoCorrect(2, 5, buildViolationMessage("Bookscreen"))
    }

    @Test
    fun `allows a composable that ends in an allowed suffix`() {
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

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows the component fallback suffix`() {
        val code =
            """
            @Composable
            fun PlayerControlsComponent() {
                Row {}
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a lowercase composable that returns a value`() {
        val code =
            """
            @Composable
            fun rememberDayProgress(): Book? = remember { null }

            @Composable
            fun chapterCountText(count: Int): String = count.toString()
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a preview named after what it previews`() {
        val code =
            """
            @Preview
            @Composable
            private fun BookRowPreview() {
                BookRow(title = "Book")
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows a multi preview annotation`() {
        val code =
            """
            @PreviewLightDark
            @Composable
            private fun BookRowPreview() {
                BookRow(title = "Book")
            }
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }

    @Test
    fun `allows an uppercase function that is not a composable`() {
        val code =
            """
            fun BookItem(title: String): Book = Book(title = title)
            """.trimIndent()

        composableNamingSuffixRuleAssertThat(code).hasNoLintViolations()
    }
}

private fun buildViolationMessage(name: String): String =
    "Composable '$name' must end with one of the allowed suffixes: Action, Badge, Banner, Bar, Box, Bubble, " +
        "Button, Card, Cell, Chip, Collector, Column, Component, Content, Dialog, Display, Drawer, Effect, Fab, " +
        "Field, Footer, Grid, Handle, Header, Heading, Hero, Icon, Image, Indicator, Item, Label, Layout, Line, " +
        "List, Menu, Message, Option, Overlay, Pane, Panel, Picker, Pill, Rail, Root, Row, Scaffold, Screen, " +
        "Section, Sheet, Sidebar, Skeleton, Slider, Snackbar, Spacer, Spinner, Surface, Switch, Text, Theme, Tile, " +
        "Title, Toggle. When none describes it, use 'Component'"
