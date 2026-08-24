package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.About
import com.pinterest.ktlint.rule.engine.core.api.RuleAutocorrectApproveHandler
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.recursiveChildren
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

private const val IMPLICIT_PARAMETER_NAME = "it"
private const val COMPOSABLE_ANNOTATION_NAME = "Composable"

/**
 * Flags a lambda that exists only to hand its parameter to a function — `{ day -> mapDay(day) }` where
 * `::mapDay` says the same thing.
 *
 * Deliberately narrow. Ktlint resolves no types, so the rule only fires on a call to a function declared
 * in the *same file*, where it can read the declaration and rule out the shapes a reference cannot take:
 * `suspend` functions (their reference does not fit a plain function type), `@Composable` functions, and
 * anything reached through a receiver. A forwarding lambda around a function from another file is left
 * alone rather than guessed at.
 */
class PreferMethodReferenceRule :
    Rule(
        ruleId = RuleId("$RULE_SET_ID:prefer-method-reference"),
        about = About(
            maintainer = "Bible Planner",
            repositoryUrl = "https://github.com/quare-tech/bible-planner-mobile-client",
            issueTrackerUrl = "https://github.com/quare-tech/bible-planner-mobile-client/issues",
        ),
    ),
    RuleAutocorrectApproveHandler {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return

        val referenceableFunctions = node
            .recursiveChildren()
            .mapNotNull { it.psi as? KtNamedFunction }
            .filter { it.isReferenceable() }
            .toList()
        if (referenceableFunctions.isEmpty()) return

        node
            .recursiveChildren()
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

    /**
     * A `suspend` function's reference has a `suspend` function type, which does not fit the plain function
     * type `let`, `map` and `forEach` declare; `@Composable` functions cannot be referenced at all.
     */
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

    /**
     * A same-file function is referenceable from the lambda when it is top level, or a member of the class
     * the lambda itself sits in — a member of a *different* class in the file needs its own receiver.
     */
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

    /**
     * The name the lambda takes as its single parameter, whether written out or left implicit as `it`. A
     * lambda that takes several parameters, or destructures them, has no single name to forward.
     */
    private fun KtLambdaExpression.findForwardedParameterName(): String? {
        val parameters = functionLiteral.valueParameters
        if (parameters.size > 1) return null
        val parameter = parameters.firstOrNull() ?: return IMPLICIT_PARAMETER_NAME
        if (parameter.destructuringDeclaration != null) return null
        return parameter.name
    }

    /**
     * The call this lambda exists only to make: its whole body, taking [parameterName] as its one and only
     * argument. Type arguments and trailing lambdas are left alone — a reference cannot carry either.
     */
    private fun KtLambdaExpression.findSingleForwardingCall(parameterName: String): KtCallExpression? {
        val call = functionLiteral.bodyExpression?.statements?.singleOrNull() as? KtCallExpression ?: return null
        if (call.typeArgumentList != null) return null
        if (call.lambdaArguments.isNotEmpty()) return null
        val argument = call.valueArguments.singleOrNull() ?: return null
        if (argument.getArgumentName() != null || argument.isSpread) return null
        val argumentName = (argument.getArgumentExpression() as? KtNameReferenceExpression)?.getReferencedName()
        return call.takeIf { argumentName == parameterName }
    }
}
