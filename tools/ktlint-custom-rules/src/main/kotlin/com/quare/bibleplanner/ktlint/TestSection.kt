package com.quare.bibleplanner.ktlint

internal enum class TestSection(
    val comment: String,
) {
    GIVEN("// Given"),
    WHEN("// When"),
    THEN("// Then"),
}
