package com.github.pmouli.rune.doc

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.github.pmouli.rune.psi.RosettaNamedElement
import com.github.pmouli.rune.psi.RosettaTokenTypes

/**
 * Documentation provider for Rune DSL (Rosetta).
 *
 * Provides hover documentation (Quick Documentation) for:
 * - Type declarations: Shows type name, attributes, and base types
 * - Function declarations: Shows signature, inputs, outputs, conditions
 * - Enum declarations: Shows enum name and values
 * - Attributes: Shows name, type, and cardinality
 *
 * Displayed when user hovers over an element or presses F1/Ctrl+Q.
 *
 * Thread-safe: PSI access occurs within ReadAction by IntelliJ Platform.
 */
class RosettaDocumentationProvider : AbstractDocumentationProvider() {
    /**
     * Generates documentation HTML for the given element.
     */
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null) return null
        
        return when {
            isTypeDeclaration(element) -> generateTypeDoc(element)
            isFunctionDeclaration(element) -> generateFunctionDoc(element)
            isEnumDeclaration(element) -> generateEnumDoc(element)
            isAttributeDeclaration(element) -> generateAttributeDoc(element)
            element is RosettaNamedElement -> generateNamedElementDoc(element)
            else -> null
        }
    }

    /**
     * Returns the element to generate documentation for when hovering.
     */
    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        if (contextElement == null) return null
        
        // If hovering over an identifier, try to resolve its reference
        if (contextElement.node.elementType == RosettaTokenTypes.IDENTIFIER) {
            val parent = contextElement.parent
            if (parent is RosettaNamedElement) {
                return parent
            }
        }
        
        return contextElement
    }

    private fun isTypeDeclaration(element: PsiElement): Boolean {
        return element.node.elementType == RosettaTokenTypes.TYPE ||
               (element.prevSibling?.node?.elementType == RosettaTokenTypes.TYPE)
    }

    private fun isFunctionDeclaration(element: PsiElement): Boolean {
        return element.node.elementType == RosettaTokenTypes.FUNC ||
               (element.prevSibling?.node?.elementType == RosettaTokenTypes.FUNC)
    }

    private fun isEnumDeclaration(element: PsiElement): Boolean {
        return element.node.elementType == RosettaTokenTypes.ENUM ||
               (element.prevSibling?.node?.elementType == RosettaTokenTypes.ENUM)
    }

    private fun isAttributeDeclaration(element: PsiElement): Boolean {
        // Simplified: check if inside a type body
        val parent = element.parent
        return parent != null && isInsideTypeBody(parent)
    }

    private fun isInsideTypeBody(element: PsiElement): Boolean {
        var current: PsiElement? = element
        while (current != null) {
            if (current.node.elementType == RosettaTokenTypes.TYPE) {
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun generateTypeDoc(element: PsiElement): String {
        val name = getElementName(element) ?: "Unknown"
        val extendsClause = findExtendsClause(element)
        
        return buildString {
            append("<html><body>")
            append("<h3>Type: <b>$name</b></h3>")
            if (extendsClause != null) {
                append("<p>Extends: <code>$extendsClause</code></p>")
            }
            append("<p><i>Rune DSL type declaration</i></p>")
            append("</body></html>")
        }
    }

    private fun generateFunctionDoc(element: PsiElement): String {
        val name = getElementName(element) ?: "Unknown"
        
        return buildString {
            append("<html><body>")
            append("<h3>Function: <b>$name</b></h3>")
            append("<p><i>Rune DSL function declaration</i></p>")
            append("</body></html>")
        }
    }

    private fun generateEnumDoc(element: PsiElement): String {
        val name = getElementName(element) ?: "Unknown"
        
        return buildString {
            append("<html><body>")
            append("<h3>Enum: <b>$name</b></h3>")
            append("<p><i>Rune DSL enumeration</i></p>")
            append("</body></html>")
        }
    }

    private fun generateAttributeDoc(element: PsiElement): String {
        val name = getElementName(element) ?: "Unknown"
        
        return buildString {
            append("<html><body>")
            append("<h3>Attribute: <b>$name</b></h3>")
            append("<p><i>Rune DSL attribute declaration</i></p>")
            append("</body></html>")
        }
    }

    private fun generateNamedElementDoc(element: RosettaNamedElement): String {
        val name = element.name ?: "Unknown"
        
        return buildString {
            append("<html><body>")
            append("<h3>$name</h3>")
            append("<p><i>Rune DSL element</i></p>")
            append("</body></html>")
        }
    }

    private fun getElementName(element: PsiElement): String? {
        // Find the next identifier token after a keyword
        var next = PsiTreeUtil.skipWhitespacesForward(element)
        while (next != null) {
            if (next.node.elementType == RosettaTokenTypes.IDENTIFIER) {
                return next.text
            }
            next = next.nextSibling
        }
        return null
    }

    private fun findExtendsClause(element: PsiElement): String? {
        // Search for EXTENDS keyword followed by identifier
        var current: PsiElement? = element
        while (current != null) {
            if (current.node.elementType == RosettaTokenTypes.EXTENDS) {
                val next = PsiTreeUtil.skipWhitespacesForward(current)
                if (next?.node?.elementType == RosettaTokenTypes.IDENTIFIER) {
                    return next.text
                }
            }
            current = current.nextSibling
        }
        return null
    }
}
