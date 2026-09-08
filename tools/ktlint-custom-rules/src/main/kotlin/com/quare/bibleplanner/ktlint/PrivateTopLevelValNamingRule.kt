package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private val lowerCamelCaseRegex = Regex("^[a-z][a-zA-Z0-9]*$")

class PrivateTopLevelValNamingRule : BiblePlannerRule("private-top-level-val-naming") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != PROPERTY) return
        if (node.parent?.elementType != FILE) return
        if (!node.hasModifier(PRIVATE_KEYWORD)) return
        if (node.hasModifier(CONST_KEYWORD)) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        val name = identifier.text
        if (name.startsWith("_") || lowerCamelCaseRegex.matches(name)) return

        emit(
            identifier.startOffset,
            "Top-level private val '$name' should use lowerCamelCase, not PascalCase or SCREAMING_SNAKE_CASE",
            false,
        )
    }
}
