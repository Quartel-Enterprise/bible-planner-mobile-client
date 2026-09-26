package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

/**
 * In a primary constructor the properties (`val` / `var`) come first and the plain parameters last, so
 * what the instance keeps reads as one block and what it only consumes while being built as another.
 *
 * It is not autocorrected: moving a parameter changes the meaning of every positional call.
 */
class ConstructorPropertyOrderRule : BiblePlannerRule("constructor-property-order") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CLASS) return
        val parameters = (node.psi as? KtClass)?.primaryConstructorParameters ?: return
        val lastPropertyIndex = parameters.indexOfLast { parameter -> parameter.hasValOrVar() }
        parameters
            .take(lastPropertyIndex + 1)
            .filterNot { parameter -> parameter.hasValOrVar() }
            .forEach { parameter ->
                emit(
                    parameter.nameIdentifier?.textOffset ?: parameter.textOffset,
                    "Constructor parameter '${parameter.name}' is not a property and should be declared after " +
                        "the 'val' / 'var' properties",
                    false,
                )
            }
    }
}
