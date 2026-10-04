package com.quare.bibleplanner.ktlint

import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

internal const val PREPARE_SCENARIO = "prepareScenario"

private const val TEST_ANNOTATION = "Test"

internal fun KtAnnotated.hasAnnotation(names: Set<String>): Boolean =
    annotationEntries.any { entry -> entry.shortName?.asString() in names }

internal fun KtNamedFunction.isTest(): Boolean =
    annotationEntries.any { entry -> entry.shortName?.asString() == TEST_ANNOTATION }

internal fun KtDeclaration.isPrepareScenario(): Boolean = this is KtNamedFunction && name == PREPARE_SCENARIO

internal fun KtNamedFunction.findTestSections(): List<Pair<TestSection, Int>> =
    collectDescendantsOfType<PsiComment>().mapNotNull { comment ->
        TestSection.entries
            .firstOrNull { section -> section.comment == comment.text.trimEnd() }
            ?.let { section -> section to comment.textOffset }
    }
