package com.quare.bibleplanner.ktlint

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter

/**
 * A `@Serializable` `*Dto` mirrors a wire format, so every field names its JSON key with `@SerialName` — renaming the
 * Kotlin property can then never change the contract — and none of them carries a default value: what the
 * server may leave out is modelled as a nullable type, and `explicitNulls = false` reads an absent key as
 * `null`.
 */
class DtoSerialNameRule : BiblePlannerRule("dto-serial-name") {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != CLASS) return
        val ktClass = node.psi as? KtClass ?: return
        if (ktClass.name?.endsWith(DTO_SUFFIX) != true || !ktClass.isSerializable()) return

        ktClass.primaryConstructorParameters
            .filter { parameter -> parameter.hasValOrVar() }
            .forEach { parameter ->
                if (!parameter.hasSerialName()) {
                    emit(
                        parameter.textOffset,
                        "DTO field '${parameter.name}' must declare its JSON key with @SerialName",
                        false,
                    )
                }
                val defaultValue = parameter.defaultValue ?: return@forEach
                emit(
                    defaultValue.textOffset,
                    "DTO field '${parameter.name}' must not have a default value; make the type nullable and let " +
                        "'explicitNulls = false' read an absent key as null",
                    false,
                )
            }
    }

    private fun KtClass.isSerializable(): Boolean =
        annotationEntries.any { annotation -> annotation.shortName?.asString() == SERIALIZABLE_ANNOTATION }

    private fun KtParameter.hasSerialName(): Boolean =
        annotationEntries.any { annotation -> annotation.shortName?.asString() == "SerialName" }

    private companion object {
        const val DTO_SUFFIX = "Dto"
        const val SERIALIZABLE_ANNOTATION = "Serializable"
    }
}
