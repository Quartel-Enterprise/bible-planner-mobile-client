package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FILE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile

class DataClassOwnFileRule : BiblePlannerRule("data-class-own-file") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != FILE) return
        val file = node.psi as? KtFile ?: return
        val declarations = file.declarations.filterIsInstance<KtClassOrObject>()
        if (declarations.size < 2) return

        declarations
            .filter { declaration -> declaration.isDataOrEnum() }
            .forEach { declaration ->
                val neighbour = declarations.first { other -> other != declaration }
                emit(
                    declaration.textOffset,
                    "'${declaration.name}' shares its file with '${neighbour.name}': every data class and enum " +
                        "lives in a file of its own, named after it",
                    false,
                )
            }
    }

    private fun KtClassOrObject.isDataOrEnum(): Boolean = this is KtClass && (isData() || isEnum())
}
