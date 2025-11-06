package com.github.pmouli.rune.psi

import com.intellij.psi.tree.IElementType
import com.github.pmouli.rune.RosettaLanguage

/**
 * Token types for Rune DSL (Rosetta) lexer.
 *
 * Defines all terminal symbols (keywords, operators, literals, identifiers)
 * recognized by the lexer and used by the parser.
 *
 * Thread-safe: IElementType instances are immutable singletons.
 */
object RosettaTokenTypes {
    // Comments
    @JvmField val COMMENT = IElementType("COMMENT", RosettaLanguage.INSTANCE)

    // Keywords - Core constructs
    @JvmField val NAMESPACE = IElementType("NAMESPACE", RosettaLanguage.INSTANCE)
    @JvmField val TYPE = IElementType("TYPE", RosettaLanguage.INSTANCE)
    @JvmField val FUNC = IElementType("FUNC", RosettaLanguage.INSTANCE)
    @JvmField val ENUM = IElementType("ENUM", RosettaLanguage.INSTANCE)
    @JvmField val CHOICE = IElementType("CHOICE", RosettaLanguage.INSTANCE)
    @JvmField val ALIAS = IElementType("ALIAS", RosettaLanguage.INSTANCE)
    @JvmField val ANNOTATION = IElementType("ANNOTATION", RosettaLanguage.INSTANCE)
    @JvmField val SCHEME = IElementType("SCHEME", RosettaLanguage.INSTANCE)
    @JvmField val CALCULATION = IElementType("CALCULATION", RosettaLanguage.INSTANCE)
    @JvmField val REPORTING = IElementType("REPORTING", RosettaLanguage.INSTANCE)

    // Keywords - Attributes and cardinality
    @JvmField val EXTENDS = IElementType("EXTENDS", RosettaLanguage.INSTANCE)
    @JvmField val CONDITION = IElementType("CONDITION", RosettaLanguage.INSTANCE)
    @JvmField val OPTIONAL = IElementType("OPTIONAL", RosettaLanguage.INSTANCE)
    @JvmField val ONE_OF = IElementType("ONE_OF", RosettaLanguage.INSTANCE)
    @JvmField val REQUIRED = IElementType("REQUIRED", RosettaLanguage.INSTANCE)

    // Keywords - Expressions and logic
    @JvmField val IF = IElementType("IF", RosettaLanguage.INSTANCE)
    @JvmField val THEN = IElementType("THEN", RosettaLanguage.INSTANCE)
    @JvmField val ELSE = IElementType("ELSE", RosettaLanguage.INSTANCE)
    @JvmField val AND = IElementType("AND", RosettaLanguage.INSTANCE)
    @JvmField val OR = IElementType("OR", RosettaLanguage.INSTANCE)
    @JvmField val NOT = IElementType("NOT", RosettaLanguage.INSTANCE)
    @JvmField val EXISTS = IElementType("EXISTS", RosettaLanguage.INSTANCE)
    @JvmField val ONLY = IElementType("ONLY", RosettaLanguage.INSTANCE)
    @JvmField val IS = IElementType("IS", RosettaLanguage.INSTANCE)
    @JvmField val ABSENT = IElementType("ABSENT", RosettaLanguage.INSTANCE)

    // Keywords - Built-in types
    @JvmField val STRING_TYPE = IElementType("STRING_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val INT_TYPE = IElementType("INT_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val NUMBER_TYPE = IElementType("NUMBER_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val BOOLEAN_TYPE = IElementType("BOOLEAN_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val DATE_TYPE = IElementType("DATE_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val TIME_TYPE = IElementType("TIME_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val DATETIME_TYPE = IElementType("DATETIME_TYPE", RosettaLanguage.INSTANCE)
    @JvmField val ZONEDDATETIME_TYPE = IElementType("ZONEDDATETIME_TYPE", RosettaLanguage.INSTANCE)

    // Keywords - Metadata and validation
    @JvmField val SYNONYM = IElementType("SYNONYM", RosettaLanguage.INSTANCE)
    @JvmField val METADATA = IElementType("METADATA", RosettaLanguage.INSTANCE)
    @JvmField val REFERENCE = IElementType("REFERENCE", RosettaLanguage.INSTANCE)
    @JvmField val ID = IElementType("ID", RosettaLanguage.INSTANCE)
    @JvmField val KEY = IElementType("KEY", RosettaLanguage.INSTANCE)

    // Operators and punctuation
    @JvmField val COLON = IElementType("COLON", RosettaLanguage.INSTANCE)
    @JvmField val SEMICOLON = IElementType("SEMICOLON", RosettaLanguage.INSTANCE)
    @JvmField val COMMA = IElementType("COMMA", RosettaLanguage.INSTANCE)
    @JvmField val DOT = IElementType("DOT", RosettaLanguage.INSTANCE)
    @JvmField val EQUALS = IElementType("EQUALS", RosettaLanguage.INSTANCE)
    @JvmField val LPAREN = IElementType("LPAREN", RosettaLanguage.INSTANCE)
    @JvmField val RPAREN = IElementType("RPAREN", RosettaLanguage.INSTANCE)
    @JvmField val LBRACKET = IElementType("LBRACKET", RosettaLanguage.INSTANCE)
    @JvmField val RBRACKET = IElementType("RBRACKET", RosettaLanguage.INSTANCE)
    @JvmField val LBRACE = IElementType("LBRACE", RosettaLanguage.INSTANCE)
    @JvmField val RBRACE = IElementType("RBRACE", RosettaLanguage.INSTANCE)
    @JvmField val LT = IElementType("LT", RosettaLanguage.INSTANCE)
    @JvmField val GT = IElementType("GT", RosettaLanguage.INSTANCE)
    @JvmField val LE = IElementType("LE", RosettaLanguage.INSTANCE)
    @JvmField val GE = IElementType("GE", RosettaLanguage.INSTANCE)
    @JvmField val EQ = IElementType("EQ", RosettaLanguage.INSTANCE)
    @JvmField val NE = IElementType("NE", RosettaLanguage.INSTANCE)
    @JvmField val PLUS = IElementType("PLUS", RosettaLanguage.INSTANCE)
    @JvmField val MINUS = IElementType("MINUS", RosettaLanguage.INSTANCE)
    @JvmField val MULT = IElementType("MULT", RosettaLanguage.INSTANCE)
    @JvmField val DIV = IElementType("DIV", RosettaLanguage.INSTANCE)
    @JvmField val ARROW = IElementType("ARROW", RosettaLanguage.INSTANCE)
    @JvmField val DOTDOT = IElementType("DOTDOT", RosettaLanguage.INSTANCE)

    // Literals and identifiers
    @JvmField val INTEGER_LITERAL = IElementType("INTEGER_LITERAL", RosettaLanguage.INSTANCE)
    @JvmField val STRING_LITERAL = IElementType("STRING_LITERAL", RosettaLanguage.INSTANCE)
    @JvmField val PATTERN_LITERAL = IElementType("PATTERN_LITERAL", RosettaLanguage.INSTANCE)
    @JvmField val IDENTIFIER = IElementType("IDENTIFIER", RosettaLanguage.INSTANCE)
}
