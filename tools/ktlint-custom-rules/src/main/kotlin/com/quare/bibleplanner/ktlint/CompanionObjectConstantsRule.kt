package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtObjectDeclaration

class CompanionObjectConstantsRule : BiblePlannerRule("companion-object-constants") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != PROPERTY) return
        if (node.hasModifier(CONST_KEYWORD)) return
        if (!node.isCompanionMember()) return
        if (!node.isPrivateMember()) return

        val identifier = node.findChildByType(IDENTIFIER) ?: return
        emit(
            identifier.startOffset,
            "Private val '${identifier.text}' should be declared in the class body: a companion object holds " +
                "constants and public API, not the class's own private values",
            false,
        )
    }

    private fun ASTNode.isCompanionMember(): Boolean {
        val companion = parent?.takeIf { parent -> parent.elementType == CLASS_BODY }?.parent ?: return false
        if (companion.elementType != OBJECT_DECLARATION) return false
        return (companion.psi as? KtObjectDeclaration)?.isCompanion() == true
    }

    private fun ASTNode.isPrivateMember(): Boolean {
        if (hasModifier(PRIVATE_KEYWORD)) return true
        val companion = parent?.parent ?: return false
        return companion.hasModifier(PRIVATE_KEYWORD)
    }
}
