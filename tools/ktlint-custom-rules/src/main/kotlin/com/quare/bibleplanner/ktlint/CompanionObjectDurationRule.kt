package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PROPERTY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

class CompanionObjectDurationRule : BiblePlannerRule("companion-object-duration") {
    private val durationUnitSuffix = Regex("""\.(nanoseconds|microseconds|milliseconds|seconds|minutes|hours|days)$""")

    private val durationFactory = Regex("""(^|\W)Duration\.|\.toDuration\(""")

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != PROPERTY) return
        val property = node.psi as? KtProperty ?: return
        val companion = property.containingClassOrObject as? KtObjectDeclaration ?: return
        if (!companion.isCompanion() || companion.isPrivate() || property.isPrivate()) return
        if (!property.isDuration()) return

        emit(
            property.textOffset,
            "Duration '${property.name}' should be a private val in the class body, not a companion object member",
            false,
        )
    }

    private fun KtProperty.isDuration(): Boolean {
        val type = typeReference?.text
        if (type != null) return type == DURATION_TYPE || type.endsWith(".$DURATION_TYPE")
        val initializer = initializer?.text ?: return false
        return durationUnitSuffix.containsMatchIn(initializer) || durationFactory.containsMatchIn(initializer)
    }

    private companion object {
        const val DURATION_TYPE = "Duration"
    }
}
