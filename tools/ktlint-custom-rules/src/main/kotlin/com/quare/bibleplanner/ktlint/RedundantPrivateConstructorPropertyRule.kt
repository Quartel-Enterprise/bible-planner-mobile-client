package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtValueArgumentName
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * A `private val` in the primary constructor that is only read while the instance is being built — by a
 * property initializer, a delegate or an `init` block — never needed to be a property: a plain constructor
 * parameter reaches the same places and keeps the class one field smaller.
 *
 * The rule has no type resolution, so it only reports what it can prove: a single read from a method, a
 * property accessor, a nested class or through a qualifier (`this.x`, `other.x`) keeps the property.
 */
class RedundantPrivateConstructorPropertyRule : BiblePlannerRule("redundant-private-constructor-property") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CLASS) return
        val ktClass = node.psi as? KtClass ?: return
        if (ktClass.isData() || ktClass.isValue() || ktClass.isAnnotation()) return

        val references = ktClass.collectDescendantsOfType<KtNameReferenceExpression>()
        ktClass.primaryConstructorParameters
            .filter { parameter -> parameter.isPrivateProperty() }
            .forEach { parameter ->
                val reads = references.filter { reference -> reference.isReadOf(parameter) }
                if (reads.isEmpty() || reads.any { read -> read.needsProperty(ktClass) }) return@forEach
                val keyword = parameter.valOrVarKeyword ?: return@forEach
                emit(
                    keyword.textOffset,
                    "Constructor property '${parameter.name}' is only read while the instance is initialized; " +
                        "drop 'private ${keyword.text}' and keep it as a plain constructor parameter",
                    false,
                )
            }
    }

    private fun KtParameter.isPrivateProperty(): Boolean = hasValOrVar() &&
        hasModifier(KtTokens.PRIVATE_KEYWORD) &&
        !hasModifier(KtTokens.OVERRIDE_KEYWORD)

    private fun KtNameReferenceExpression.isReadOf(parameter: KtParameter): Boolean =
        getReferencedName() == parameter.name && parent !is KtValueArgumentName

    private fun KtNameReferenceExpression.needsProperty(owner: KtClass): Boolean {
        val qualified = parent as? KtQualifiedExpression
        if (qualified != null && qualified.selectorExpression == this) return true
        return parents
            .takeWhile { ancestor -> ancestor != owner }
            .any { ancestor -> ancestor.isOutlivingInitialization() }
    }

    private fun PsiElement.isOutlivingInitialization(): Boolean = this is KtNamedFunction ||
        this is KtPropertyAccessor ||
        this is KtSecondaryConstructor ||
        this is KtClassOrObject
}
