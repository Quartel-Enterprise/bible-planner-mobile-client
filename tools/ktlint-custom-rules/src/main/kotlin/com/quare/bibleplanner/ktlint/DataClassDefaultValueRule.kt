package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

class DataClassDefaultValueRule : BiblePlannerRule("data-class-default-value") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CLASS) return
        val ktClass = node.psi as? KtClass ?: return
        if (!ktClass.isData() || ktClass.isSerializableDto()) return

        ktClass.primaryConstructorParameters
            .filter { parameter -> parameter.defaultValue != null }
            .forEach { parameter ->
                emit(
                    parameter.textOffset,
                    "Data class property '${parameter.name}' must not have a default value: pass it explicitly " +
                        "wherever the class is built",
                    false,
                )
            }
    }

    private fun KtClass.isSerializableDto(): Boolean = name?.endsWith(DTO_SUFFIX) == true &&
        annotationEntries.any { annotation -> annotation.shortName?.asString() == SERIALIZABLE_ANNOTATION }

    private companion object {
        const val DTO_SUFFIX = "Dto"
        const val SERIALIZABLE_ANNOTATION = "Serializable"
    }
}
