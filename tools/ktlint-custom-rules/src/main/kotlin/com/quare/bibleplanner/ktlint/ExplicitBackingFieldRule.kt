package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EQ
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VAL_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace
import com.pinterest.ktlint.rule.engine.core.api.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class ExplicitBackingFieldRule : BiblePlannerRule("explicit-backing-field") {
    private val exposureRegex = Regex("^(_[a-zA-Z]\\w*)(\\.as(State|Shared)Flow\\(\\))?$")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != PROPERTY) return
        if (node.parent?.elementType != CLASS_BODY) return

        val name = node.findChildByType(IDENTIFIER)?.text ?: return
        val backingName = node
            .findInitializerText()
            ?.let(exposureRegex::matchEntire)
            ?.groupValues
            ?.get(1)
            ?: return
        if (backingName != "_$name") return

        val backingIdentifier = node.treeParent
            .getChildren(null)
            .firstOrNull { sibling -> sibling.isPrivateValNamed(backingName) }
            ?.findChildByType(IDENTIFIER)
            ?: return

        emit(
            backingIdentifier.startOffset,
            "Backing property '$backingName' only exposes '$name' — declare '$name' with an explicit backing " +
                "field (field = ...) instead",
            false,
        )
    }

    private fun ASTNode.findInitializerText(): String? {
        var candidate = findChildByType(EQ)?.treeNext
        while (candidate != null && candidate.isWhiteSpace()) {
            candidate = candidate.treeNext
        }
        return candidate?.text
    }

    private fun ASTNode.isPrivateValNamed(backingName: String): Boolean = elementType == PROPERTY &&
        findChildByType(VAL_KEYWORD) != null &&
        hasModifier(PRIVATE_KEYWORD) &&
        findChildByType(IDENTIFIER)?.text == backingName
}
