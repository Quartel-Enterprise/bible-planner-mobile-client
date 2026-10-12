package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.INTEGER_CONSTANT
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression

class ManualTimeUnitConversionRule : BiblePlannerRule("manual-time-unit-conversion") {
    private val clockFactors = setOf(24L, 60L, 3_600L)

    private val timeFactors = clockFactors + 1_000L

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != BINARY_EXPRESSION) return
        val expression = node.psi as? KtBinaryExpression ?: return
        if (!expression.isMultiplication() || expression.isInsideMultiplication()) return
        val factors = expression.collectFactors().mapNotNull { factor -> factor.toIntegerValue() }
        val conversionFactors = factors.filter { factor -> factor in timeFactors }.toSet()
        if (conversionFactors.size < 2 || conversionFactors.none { factor -> factor in clockFactors }) return

        emit(
            node.startOffset,
            "'${expression.text}' converts time units by hand: use a kotlin.time.Duration (minutes, hours, days) " +
                "and convert with inWholeMilliseconds or inWholeSeconds only where an API takes a number",
            false,
        )
    }

    private fun KtBinaryExpression.isMultiplication(): Boolean = operationToken == KtTokens.MUL

    private fun KtExpression.isInsideMultiplication(): Boolean {
        var parent = parent
        while (parent is KtParenthesizedExpression) parent = parent.parent
        return (parent as? KtBinaryExpression)?.isMultiplication() == true
    }

    private fun KtExpression.collectFactors(): List<KtExpression> {
        if (this is KtParenthesizedExpression) return expression?.collectFactors().orEmpty()
        if (this !is KtBinaryExpression || !isMultiplication()) return listOf(this)
        return left?.collectFactors().orEmpty() + right?.collectFactors().orEmpty()
    }

    private fun KtExpression.toIntegerValue(): Long? {
        if (this !is KtConstantExpression) return null
        val digits = text.replace("_", "").trimEnd('L', 'l', 'U', 'u')
        return when (node.elementType) {
            INTEGER_CONSTANT -> digits.toLongOrNull()

            FLOAT_CONSTANT ->
                digits
                    .trimEnd('F', 'f')
                    .toDoubleOrNull()
                    ?.takeIf { value -> value % 1 == 0.0 }
                    ?.toLong()

            else -> null
        }
    }
}
