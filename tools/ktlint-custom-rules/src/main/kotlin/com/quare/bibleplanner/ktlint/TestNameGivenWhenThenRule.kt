package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction

class TestNameGivenWhenThenRule : BiblePlannerRule("test-name-given-when-then") {
    private val namePattern = Regex("GIVEN .+ WHEN .+ THEN .+")

    // Why: device tests are dexed, and D8 rejects any other character in a method name (an apostrophe, a comma).
    private val allowedCharacters = Regex("[A-Za-z0-9 _-]+")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        val function = node.psi as? KtNamedFunction ?: return
        if (!function.isTest()) return
        val name = function.name ?: return
        val identifier = function.nameIdentifier ?: return

        val message = if (!namePattern.matches(name)) {
            "Test '$name' is not named 'GIVEN <state> WHEN <action> THEN <outcome>'"
        } else if (!allowedCharacters.matches(name)) {
            "Test '$name' may only hold letters, digits, spaces, '-' and '_': D8 rejects any other character " +
                "when the test is dexed for a device"
        } else {
            return
        }
        emit(
            identifier.textOffset,
            message,
            false,
        )
    }
}
