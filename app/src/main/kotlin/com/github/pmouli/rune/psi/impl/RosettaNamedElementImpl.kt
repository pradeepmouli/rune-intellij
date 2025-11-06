package com.github.pmouli.rune.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.github.pmouli.rune.psi.RosettaNamedElement
import com.github.pmouli.rune.psi.RosettaPsiElementFactory

/**
 * Base implementation for named PSI elements in Rune DSL.
 *
 * Provides common functionality for elements that can be referenced by name,
 * such as type declarations, function declarations, and attributes.
 *
 * Thread-safe: PSI elements are immutable and accessed via ReadAction.
 */
abstract class RosettaNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), RosettaNamedElement {
    /**
     * Returns the name of this element by finding the identifier token.
     * Subclasses can override to provide custom name resolution logic.
     */
    override fun getName(): String? {
        return nameIdentifier?.text
    }

    /**
     * Returns the identifier element representing the name of this element.
     * By default, finds the first IDENTIFIER token in the subtree.
     */
    override fun getNameIdentifier(): PsiElement? {
        // Find first IDENTIFIER token in this element's subtree
        return PsiTreeUtil.findChildOfAnyType(this, PsiElement::class.java)?.let { child ->
            if (child.node.elementType.toString().contains("IDENTIFIER")) child else null
        }
    }

    /**
     * Renames this element by replacing the identifier token.
     * Used by the rename refactoring feature.
     */
    override fun setName(name: String): RosettaNamedElement {
        val newIdentifier = RosettaPsiElementFactory.createIdentifier(project, name)
        nameIdentifier?.replace(newIdentifier)
        return this
    }

    /**
     * Returns the text offset of the name identifier for navigation purposes.
     */
    override fun getTextOffset(): Int {
        return nameIdentifier?.textOffset ?: super.getTextOffset()
    }
}
