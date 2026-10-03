package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.recursiveChildren20
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens.SUSPEND_KEYWORD
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

class PreferMethodReferenceRule : BiblePlannerRule("prefer-method-reference") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return

        val referenceableFunctions = node
            .recursiveChildren20
            .mapNotNull { it.psi as? KtNamedFunction }
            .filter { it.isReferenceable() }
            .toList()
        if (referenceableFunctions.isEmpty()) return

        node
            .recursiveChildren20
            .filter { it.elementType == LAMBDA_EXPRESSION }
            .mapNotNull { it.psi as? KtLambdaExpression }
            .forEach { lambda ->
                val calleeName = lambda.findForwardedCalleeName(referenceableFunctions) ?: return@forEach
                emit(
                    lambda.node.startOffset,
                    "Lambda only forwards its parameter to '$calleeName' — pass a method reference " +
                        "(::$calleeName) instead of wrapping the call in a lambda",
                    false,
                )
            }
    }

    private fun KtNamedFunction.isReferenceable(): Boolean {
        if (hasModifier(SUSPEND_KEYWORD)) return false
        if (annotationEntries.any { it.shortName?.asString() == COMPOSABLE_ANNOTATION_NAME }) return false
        return name != null && !isLocal
    }

    private fun KtLambdaExpression.findForwardedCalleeName(referenceableFunctions: List<KtNamedFunction>): String? {
        if (isInsideComposable()) return null
        val parameterName = findForwardedParameterName() ?: return null
        val call = findSingleForwardingCall(parameterName) ?: return null
        val calleeName = (call.calleeExpression as? KtNameReferenceExpression)?.getReferencedName() ?: return null
        val declaration = referenceableFunctions.firstOrNull { it.name == calleeName } ?: return null
        return calleeName.takeIf { declaration.isReachableFrom(this) }
    }

    private fun KtNamedFunction.isReachableFrom(lambda: KtLambdaExpression): Boolean {
        val declaringClass = containingClassOrObject ?: return true
        return lambda.findEnclosingClasses().any { it == declaringClass }
    }

    private fun KtLambdaExpression.findEnclosingClasses(): Sequence<KtClassOrObject> =
        generateSequence(parent) { element -> element.parent }
            .takeWhile { element -> element !is KtFile }
            .filterIsInstance<KtClassOrObject>()

    private fun KtLambdaExpression.isInsideComposable(): Boolean {
        var current: PsiElement? = parent
        while (current != null) {
            val isComposable = (current as? KtNamedFunction)
                ?.annotationEntries
                ?.any { annotation -> annotation.shortName?.asString() == COMPOSABLE_ANNOTATION_NAME }
            if (isComposable == true) return true
            current = current.parent
        }
        return false
    }

    private fun KtLambdaExpression.findForwardedParameterName(): String? {
        val parameters = functionLiteral.valueParameters
        if (parameters.size > 1) return null
        val parameter = parameters.firstOrNull() ?: return IMPLICIT_PARAMETER_NAME
        if (parameter.destructuringDeclaration != null) return null
        return parameter.name
    }

    private fun KtLambdaExpression.findSingleForwardingCall(parameterName: String): KtCallExpression? {
        val call = functionLiteral.bodyExpression?.statements?.singleOrNull() as? KtCallExpression ?: return null
        if (call.typeArgumentList != null) return null
        if (call.lambdaArguments.isNotEmpty()) return null
        val argument = call.valueArguments.singleOrNull() ?: return null
        if (argument.getArgumentName() != null || argument.isSpread) return null
        val argumentName = (argument.getArgumentExpression() as? KtNameReferenceExpression)?.getReferencedName()
        return call.takeIf { argumentName == parameterName }
    }

    private companion object {
        const val IMPLICIT_PARAMETER_NAME = "it"
        const val COMPOSABLE_ANNOTATION_NAME = "Composable"
    }
}
