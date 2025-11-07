package dev.mouli.rune.reference

import dev.mouli.rune.psi.RosettaFile
import dev.mouli.rune.psi.RosettaNamedElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil

/**
 * Reference implementation for Rune DSL (Rosetta) identifiers.
 *
 * Resolves references to:
 * - Type declarations (in type references, extends clauses)
 * - Function declarations (in function calls)
 * - Attribute declarations (in expressions)
 * - Enum values (in expressions)
 *
 * Supports:
 * - Go to Definition (Ctrl/Cmd+Click)
 * - Find Usages
 * - Rename refactoring
 *
 * Thread-safe: PSI access occurs within ReadAction by IntelliJ Platform.
 */
class RosettaReference(
    element: PsiElement,
    textRange: TextRange,
) : PsiReferenceBase<PsiElement>(element, textRange), PsiPolyVariantReference {
    /**
     * Returns the name being referenced (the identifier text).
     */
    private val referenceName: String
        get() = element.text.substring(rangeInElement.startOffset, rangeInElement.endOffset)

    /**
     * Resolves the reference to its declaration.
     * Returns null if the reference cannot be resolved.
     */
    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results[0].element else null
    }

    /**
     * Resolves the reference to all possible declarations.
     * Used for languages with overloading or ambiguous references.
     */
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val file = element.containingFile as? RosettaFile ?: return ResolveResult.EMPTY_ARRAY

        // Search for named elements in the file and project
        val declarations = findDeclarationsInFile(file)

        return declarations
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    /**
     * Finds all declarations with the given name in the file.
     */
    private fun findDeclarationsInFile(file: RosettaFile): List<RosettaNamedElement> {
        val result = mutableListOf<RosettaNamedElement>()

        // Find all named elements in the file
        PsiTreeUtil.findChildrenOfType(file, RosettaNamedElement::class.java).forEach { namedElement ->
            if (namedElement.name == referenceName) {
                result.add(namedElement)
            }
        }

        return result
    }

    /**
     * Returns completion variants for this reference position.
     * Used by code completion to suggest valid identifiers.
     */
    override fun getVariants(): Array<Any> {
        val file = element.containingFile as? RosettaFile ?: return emptyArray()

        // Return all named elements in the file as completion suggestions
        return PsiTreeUtil.findChildrenOfType(file, RosettaNamedElement::class.java)
            .mapNotNull { it.name }
            .toTypedArray()
    }

    /**
     * Simple resolve result wrapper.
     */
    private class PsiElementResolveResult(private val psiElement: PsiElement) : ResolveResult {
        override fun getElement(): PsiElement = psiElement

        override fun isValidResult(): Boolean = true
    }
}
