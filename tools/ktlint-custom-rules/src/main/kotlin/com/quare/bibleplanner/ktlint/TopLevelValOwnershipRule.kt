package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.rule.engine.core.api.children20
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty

/**
 * A top-level `private val` / `private const val` that only one top-level class or object ever reads belongs to
 * that type: constants in its `private companion object`, everything else in its class body.
 *
 * The rule works off the reference sites, which is what keeps the documented exceptions out of it without any
 * type resolution: a constant read by a top-level `@Composable` has a reference outside every class, a default
 * for a constructor parameter is read from the primary constructor, and a file with no class has no owner to
 * move anything into.
 */
class TopLevelValOwnershipRule : BiblePlannerRule("top-level-val-ownership") {
    private val classifierElementTypes = setOf(CLASS, OBJECT_DECLARATION)

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return

        val classifiers = node.children20.filter { child -> child.elementType in classifierElementTypes }.toSet()
        if (classifiers.isEmpty()) return
        node.children20
            .filter { child -> child.isTopLevelPrivateVal() }
            .toList()
            .forEach { property -> property.reportWhenASingleClassOwnsIt(node, classifiers, emit) }
    }

    private fun ASTNode.reportWhenASingleClassOwnsIt(
        file: ASTNode,
        classifiers: Set<ASTNode>,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val identifier = findChildByType(IDENTIFIER) ?: return
        val references = file.findReferencesTo(identifier.text).filterNot { reference -> reference.isInside(this) }
        if (references.isEmpty()) return

        val owner = references.map { reference -> reference.findTopLevelAncestor() }.distinct().singleOrNull()
        if (owner == null || owner !in classifiers || !owner.canHoldProperties()) return
        if (references.any { reference -> reference.isInsideConstructorScopeOf(owner) }) return

        val ownerName = owner.findChildByType(IDENTIFIER)?.text ?: return
        val message = buildViolationMessage(
            name = identifier.text,
            ownerName = ownerName,
            isConstant = hasModifier(CONST_KEYWORD),
        )
        emit(identifier.startOffset, message, false)
    }

    private fun buildViolationMessage(
        name: String,
        ownerName: String,
        isConstant: Boolean,
    ): String = if (isConstant) {
        "Top-level private const val '$name' is only read by '$ownerName' and should be declared in its " +
            "private companion object"
    } else {
        "Top-level private val '$name' is only read by '$ownerName' and should be declared in its class body"
    }

    private fun ASTNode.isTopLevelPrivateVal(): Boolean = elementType == PROPERTY &&
        hasModifier(PRIVATE_KEYWORD) &&
        (psi as? KtProperty)?.receiverTypeReference == null

    private fun ASTNode.findReferencesTo(name: String): List<ASTNode> = children20
        .flatMap { child ->
            if (child.elementType == REFERENCE_EXPRESSION && child.text == name) {
                sequenceOf(child)
            } else {
                child.findReferencesTo(name).asSequence()
            }
        }.toList()

    private fun ASTNode.findTopLevelAncestor(): ASTNode? {
        var current = this
        while (true) {
            val parentNode = current.parent ?: return null
            if (parentNode.elementType == FILE) return current
            current = parentNode
        }
    }

    private fun ASTNode.isInside(ancestor: ASTNode): Boolean {
        var current: ASTNode? = this
        while (current != null) {
            if (current == ancestor) return true
            current = current.parent
        }
        return false
    }

    private fun ASTNode.isInsideConstructorScopeOf(owner: ASTNode): Boolean {
        var current: ASTNode? = this
        while (current != null && current != owner) {
            if (current.elementType == PRIMARY_CONSTRUCTOR || current.elementType == SUPER_TYPE_LIST) return true
            current = current.parent
        }
        return false
    }

    private fun ASTNode.canHoldProperties(): Boolean {
        val ktClass = psi as? KtClass ?: return true
        return !ktClass.isInterface() && !ktClass.isAnnotation() && !hasModifier(KtTokens.VALUE_KEYWORD)
    }
}
