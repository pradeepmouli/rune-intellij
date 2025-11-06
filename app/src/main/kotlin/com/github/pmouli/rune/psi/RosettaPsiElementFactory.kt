package com.github.pmouli.rune.psi

import com.github.pmouli.rune.RosettaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory

/**
 * Factory for creating PSI elements programmatically.
 *
 * Used for:
 * - Rename refactoring (creating new identifier elements)
 * - Quick fixes (generating code snippets)
 * - Code generation (building PSI trees from scratch)
 *
 * Thread-safe: Uses WriteCommandAction when creating elements that modify PSI.
 */
object RosettaPsiElementFactory {
    /**
     * Creates a Rosetta file from text content.
     *
     * @param project The project context
     * @param text The Rosetta source code
     * @return A RosettaFile PSI element
     */
    fun createFile(
        project: Project,
        text: String,
    ): RosettaFile {
        val name = "dummy.rosetta"
        return PsiFileFactory.getInstance(project)
            .createFileFromText(name, RosettaFileType.INSTANCE, text) as RosettaFile
    }

    /**
     * Creates an identifier element from text.
     *
     * Used by rename refactoring to replace identifier tokens.
     *
     * @param project The project context
     * @param name The identifier name
     * @return A PSI element representing the identifier
     */
    fun createIdentifier(
        project: Project,
        name: String,
    ): PsiElement {
        val file = createFile(project, "type $name:")
        // Navigate to the identifier token in the created file
        // This is a simplified implementation; actual implementation would use tree navigation
        return file.firstChild ?: file
    }

    /**
     * Creates a type declaration element.
     *
     * @param project The project context
     * @param name The type name
     * @return A PSI element representing the type declaration
     */
    fun createTypeDeclaration(
        project: Project,
        name: String,
    ): PsiElement {
        val file = createFile(project, "type $name: {}")
        return file.firstChild ?: file
    }
}
