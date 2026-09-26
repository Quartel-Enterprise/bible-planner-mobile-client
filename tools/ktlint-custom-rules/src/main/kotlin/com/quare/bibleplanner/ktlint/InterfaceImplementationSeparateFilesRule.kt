package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SEALED_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile

class InterfaceImplementationSeparateFilesRule : BiblePlannerRule("interface-implementation-separate-files") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return
        val file = node.psi as? KtFile ?: return
        val interfaces = file.declarations
            .filterIsInstance<KtClass>()
            .filter { declaration -> declaration.isSeparableInterface() }
        if (interfaces.isEmpty()) return
        val interfaceNames = interfaces.mapNotNull { declaration -> declaration.name }.toSet()

        PsiTreeUtil
            .collectElementsOfType(file, KtClassOrObject::class.java)
            .filterNot { candidate -> candidate.isNestedInAny(interfaces) }
            .forEach { candidate ->
                val implemented = candidate.superTypeListEntries
                    .mapNotNull { entry -> entry.typeAsUserType?.referencedName }
                    .firstOrNull { name -> name in interfaceNames } ?: return@forEach
                val identifier = candidate.node.findChildByType(IDENTIFIER) ?: return@forEach
                emit(
                    identifier.startOffset,
                    "Class '${identifier.text}' implements '$implemented', which is declared in this same file: " +
                        "keep an interface and its implementation in separate files",
                    false,
                )
            }
    }

    private fun KtClass.isSeparableInterface(): Boolean = isInterface() &&
        !node.hasModifier(SEALED_KEYWORD) &&
        !node.hasModifier(PRIVATE_KEYWORD)

    private fun KtClassOrObject.isNestedInAny(interfaces: List<KtClass>): Boolean = interfaces
        .any { declaration -> PsiTreeUtil.isAncestor(declaration, this, false) }
}
