package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtThrowExpression

class UnitFunctionBlockBodyRule : BiblePlannerRule("unit-function-block-body") {
    private val nothingFunctions = setOf("error", "TODO")
    private val testRunners = setOf("runTest")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return

        val function = node.psi as? KtNamedFunction ?: return
        val name = function.name ?: return
        val body = function.findExpressionBody() ?: return
        if (function.isTest() || body.isTestRun() || body.isNothing()) return

        val message = when (function.typeReference?.text?.removePrefix("$KOTLIN_PACKAGE.")) {
            null -> {
                "Function '$name' uses an expression body without a return type; declare the return " +
                    "type, or open a block body ({ … }) if it returns 'Unit'"
            }

            UNIT -> {
                "Function '$name' returns 'Unit' but uses an expression body; open a block body " +
                    "({ … }) instead of assigning with '='"
            }

            else -> return
        }
        val identifier = node.findChildByType(IDENTIFIER) ?: return
        emit(
            identifier.startOffset,
            message,
            false,
        )
    }

    private fun KtNamedFunction.findExpressionBody(): KtExpression? = bodyExpression?.takeIf { !hasBlockBody() }

    private fun KtNamedFunction.isTest(): Boolean = annotationEntries.any { it.shortName?.asString() == TEST }

    private fun KtExpression.isTestRun(): Boolean = findCalleeName() in testRunners

    private fun KtExpression.isNothing(): Boolean = this is KtThrowExpression || findCalleeName() in nothingFunctions

    private fun KtExpression.findCalleeName(): String? {
        val call = if (this is KtDotQualifiedExpression && receiverExpression.text == KOTLIN_PACKAGE) {
            selectorExpression
        } else {
            this
        }
        return (call as? KtCallExpression)?.calleeExpression?.text
    }

    private companion object {
        const val UNIT = "Unit"
        const val TEST = "Test"
        const val KOTLIN_PACKAGE = "kotlin"
    }
}
