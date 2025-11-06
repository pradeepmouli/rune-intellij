package com.github.pmouli.rune.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.github.pmouli.rune.lexer.RosettaLexerAdapter
import com.github.pmouli.rune.psi.RosettaTokenTypes

/**
 * Syntax highlighter for Rune DSL (Rosetta).
 *
 * Maps token types to text attributes (colors, font styles) for syntax highlighting.
 * Follows IntelliJ Platform color scheme conventions.
 *
 * Thread-safe: Uses immutable token sets and text attribute keys.
 */
class RosettaSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        // Define text attribute keys for different token categories
        val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val STRING = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_STRING",
            DefaultLanguageHighlighterColors.STRING
        )

        val NUMBER = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
        )

        val COMMENT = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )

        val IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER
        )

        val OPERATOR = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_OPERATOR",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
        )

        val BRACES = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_BRACES",
            DefaultLanguageHighlighterColors.BRACES
        )

        val BRACKETS = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
        )

        val PARENTHESES = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_PARENTHESES",
            DefaultLanguageHighlighterColors.PARENTHESES
        )

        val DOT = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_DOT",
            DefaultLanguageHighlighterColors.DOT
        )

        val COMMA = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_COMMA",
            DefaultLanguageHighlighterColors.COMMA
        )

        val SEMICOLON = TextAttributesKey.createTextAttributesKey(
            "ROSETTA_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
        )

        // Keyword token arrays for fast lookup
        private val KEYWORD_TOKENS = arrayOf(
            RosettaTokenTypes.NAMESPACE,
            RosettaTokenTypes.TYPE,
            RosettaTokenTypes.FUNC,
            RosettaTokenTypes.ENUM,
            RosettaTokenTypes.CHOICE,
            RosettaTokenTypes.ALIAS,
            RosettaTokenTypes.ANNOTATION,
            RosettaTokenTypes.SCHEME,
            RosettaTokenTypes.CALCULATION,
            RosettaTokenTypes.REPORTING,
            RosettaTokenTypes.EXTENDS,
            RosettaTokenTypes.CONDITION,
            RosettaTokenTypes.OPTIONAL,
            RosettaTokenTypes.ONE_OF,
            RosettaTokenTypes.REQUIRED,
            RosettaTokenTypes.IF,
            RosettaTokenTypes.THEN,
            RosettaTokenTypes.ELSE,
            RosettaTokenTypes.AND,
            RosettaTokenTypes.OR,
            RosettaTokenTypes.NOT,
            RosettaTokenTypes.EXISTS,
            RosettaTokenTypes.ONLY,
            RosettaTokenTypes.IS,
            RosettaTokenTypes.ABSENT,
            RosettaTokenTypes.STRING_TYPE,
            RosettaTokenTypes.INT_TYPE,
            RosettaTokenTypes.NUMBER_TYPE,
            RosettaTokenTypes.BOOLEAN_TYPE,
            RosettaTokenTypes.DATE_TYPE,
            RosettaTokenTypes.TIME_TYPE,
            RosettaTokenTypes.DATETIME_TYPE,
            RosettaTokenTypes.ZONEDDATETIME_TYPE,
            RosettaTokenTypes.SYNONYM,
            RosettaTokenTypes.METADATA,
            RosettaTokenTypes.REFERENCE,
            RosettaTokenTypes.ID,
            RosettaTokenTypes.KEY
        )

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val BRACES_KEYS = arrayOf(BRACES)
        private val BRACKETS_KEYS = arrayOf(BRACKETS)
        private val PARENTHESES_KEYS = arrayOf(PARENTHESES)
        private val DOT_KEYS = arrayOf(DOT)
        private val COMMA_KEYS = arrayOf(COMMA)
        private val SEMICOLON_KEYS = arrayOf(SEMICOLON)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }

    override fun getHighlightingLexer(): Lexer = RosettaLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            in KEYWORD_TOKENS -> KEYWORD_KEYS
            RosettaTokenTypes.STRING_LITERAL, RosettaTokenTypes.PATTERN_LITERAL -> STRING_KEYS
            RosettaTokenTypes.INTEGER_LITERAL -> NUMBER_KEYS
            RosettaTokenTypes.COMMENT -> COMMENT_KEYS
            RosettaTokenTypes.IDENTIFIER -> IDENTIFIER_KEYS
            RosettaTokenTypes.EQUALS, RosettaTokenTypes.LT, RosettaTokenTypes.GT,
            RosettaTokenTypes.LE, RosettaTokenTypes.GE, RosettaTokenTypes.EQ,
            RosettaTokenTypes.NE, RosettaTokenTypes.PLUS, RosettaTokenTypes.MINUS,
            RosettaTokenTypes.MULT, RosettaTokenTypes.DIV, RosettaTokenTypes.ARROW,
            RosettaTokenTypes.DOTDOT -> OPERATOR_KEYS
            RosettaTokenTypes.LBRACE, RosettaTokenTypes.RBRACE -> BRACES_KEYS
            RosettaTokenTypes.LBRACKET, RosettaTokenTypes.RBRACKET -> BRACKETS_KEYS
            RosettaTokenTypes.LPAREN, RosettaTokenTypes.RPAREN -> PARENTHESES_KEYS
            RosettaTokenTypes.DOT -> DOT_KEYS
            RosettaTokenTypes.COMMA -> COMMA_KEYS
            RosettaTokenTypes.SEMICOLON, RosettaTokenTypes.COLON -> SEMICOLON_KEYS
            else -> EMPTY_KEYS
        }
    }
}
