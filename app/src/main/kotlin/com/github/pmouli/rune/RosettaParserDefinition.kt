package com.github.pmouli.rune

import com.github.pmouli.rune.lexer.RosettaLexerAdapter
import com.github.pmouli.rune.parser.RosettaParser
import com.github.pmouli.rune.psi.RosettaFile
import com.github.pmouli.rune.psi.RosettaTokenTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Parser definition for Rune DSL (Rosetta).
 *
 * Connects the lexer, parser, and PSI elements for IntelliJ Platform.
 * Defines token sets for comments, strings, and whitespace to enable
 * proper IDE features (e.g., comment handling, string manipulation).
 *
 * Thread-safe: all methods operate on immutable configurations or create new instances.
 */
class RosettaParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(RosettaLanguage.INSTANCE)

        val COMMENTS = TokenSet.create(RosettaTokenTypes.COMMENT)

        val STRINGS =
            TokenSet.create(
                RosettaTokenTypes.STRING_LITERAL,
                RosettaTokenTypes.PATTERN_LITERAL,
            )

        val WHITESPACE = TokenSet.EMPTY // Handled by lexer returning WHITE_SPACE token
    }

    override fun createLexer(project: Project): Lexer = RosettaLexerAdapter()

    override fun createParser(project: Project): PsiParser = RosettaParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun getWhitespaceTokens(): TokenSet = WHITESPACE

    override fun createElement(node: ASTNode): PsiElement {
        // Grammar-Kit will generate RosettaElementTypes.Factory.createElement
        // For now, return a basic wrapper
        return com.intellij.extapi.psi.ASTWrapperPsiElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile = RosettaFile(viewProvider)
}
