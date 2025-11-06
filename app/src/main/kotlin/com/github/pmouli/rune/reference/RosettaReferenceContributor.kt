package com.github.pmouli.rune.reference

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import com.github.pmouli.rune.psi.RosettaTokenTypes

/**
 * Reference contributor for Rune DSL (Rosetta).
 *
 * Registers reference providers for different contexts where identifiers
 * can reference declarations (types, functions, attributes, etc.).
 *
 * Enables IDE features:
 * - Go to Definition (Ctrl/Cmd+Click on identifier)
 * - Find Usages (Alt+F7 on declaration)
 * - Rename Refactoring (Shift+F6)
 *
 * Thread-safe: Reference resolution occurs within ReadAction.
 */
class RosettaReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Register reference provider for all IDENTIFIER tokens
        // This will enable go-to-definition for type references, function calls, etc.
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(RosettaTokenTypes.IDENTIFIER),
            IdentifierReferenceProvider()
        )
    }

    /**
     * Provider for identifier references.
     */
    private class IdentifierReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(
            element: PsiElement,
            context: ProcessingContext
        ): Array<PsiReference> {
            // Create a reference for the entire identifier text
            val textRange = TextRange(0, element.textLength)
            return arrayOf(RosettaReference(element, textRange))
        }
    }
}
