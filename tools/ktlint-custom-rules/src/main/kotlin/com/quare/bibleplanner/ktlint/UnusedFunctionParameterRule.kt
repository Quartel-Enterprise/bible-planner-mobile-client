package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtValueArgumentName
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isAncestor

/**
 * A parameter the function never reads is a promise the signature does not keep: every caller has to
 * produce a value that changes nothing.
 *
 * Functions whose signature is dictated from outside are skipped — `override`, `open`, `abstract`,
 * `operator`, `expect` / `actual`, `external` and interface members — as is anything annotated with
 * `@Suppress("UNUSED_PARAMETER")`.
 */
class UnusedFunctionParameterRule : BiblePlannerRule("unused-function-parameter") {
    private val externallyDictatedModifiers = listOf(
        KtTokens.OVERRIDE_KEYWORD,
        KtTokens.OPEN_KEYWORD,
        KtTokens.ABSTRACT_KEYWORD,
        KtTokens.OPERATOR_KEYWORD,
        KtTokens.EXPECT_KEYWORD,
        KtTokens.ACTUAL_KEYWORD,
        KtTokens.EXTERNAL_KEYWORD,
    )

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FUN) return
        val function = node.psi as? KtNamedFunction ?: return
        if (!function.hasBody() || function.hasDictatedSignature() || function.isSuppressed()) return

        val readNames = function
            .collectDescendantsOfType<KtNameReferenceExpression>()
            .filter { reference -> reference.parent !is KtValueArgumentName }
        function.valueParameters.forEach { parameter ->
            val isRead = readNames.any { reference ->
                reference.getReferencedName() == parameter.name && !parameter.isAncestor(reference)
            }
            if (isRead) return@forEach
            emit(
                parameter.nameIdentifier?.textOffset ?: parameter.textOffset,
                "Parameter '${parameter.name}' is never used by '${function.name}'; remove it",
                false,
            )
        }
    }

    private fun KtNamedFunction.hasDictatedSignature(): Boolean =
        externallyDictatedModifiers.any { modifier -> hasModifier(modifier) } ||
            (containingClassOrObject as? KtClass)?.isInterface() == true

    private fun KtNamedFunction.isSuppressed(): Boolean = annotationEntries.any { annotation ->
        annotation.shortName?.asString() == "Suppress" && annotation.text.contains(SUPPRESSION_NAME)
    }

    private companion object {
        const val SUPPRESSION_NAME = "UNUSED_PARAMETER"
    }
}
