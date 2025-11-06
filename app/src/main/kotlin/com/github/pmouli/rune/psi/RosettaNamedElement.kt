package com.github.pmouli.rune.psi

import com.intellij.psi.PsiNamedElement

/**
 * Base interface for named Rune DSL (Rosetta) PSI elements.
 *
 * Represents PSI elements that can be referenced by name, such as:
 * - Type declarations (type Foo)
 * - Function declarations (func Bar)
 * - Enum declarations (enum Status)
 * - Attributes (attrName string)
 *
 * Extends PsiNamedElement to enable IDE features like Find Usages,
 * Rename Refactoring, and Go to Definition.
 *
 * Thread-safe: PSI elements are immutable and accessed via ReadAction.
 */
interface RosettaNamedElement : RosettaPsiElement, PsiNamedElement {
    /**
     * Returns the name of this element for display and navigation purposes.
     * Required by PsiNamedElement.
     */
    override fun getName(): String?

    /**
     * Sets the name of this element (used by rename refactoring).
     * Must use WriteCommandAction for PSI modifications.
     */
    override fun setName(name: String): RosettaNamedElement
}
