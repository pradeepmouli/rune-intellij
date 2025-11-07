package dev.mouli.rune.lexer

import com.intellij.lexer.FlexAdapter

/**
 * Lexer adapter for Rune DSL (Rosetta).
 *
 * Wraps the JFlex-generated RosettaLexer for use with IntelliJ Platform.
 * The actual lexer implementation is generated from RosettaLexer.flex
 * by the Grammar-Kit/JFlex plugin during build.
 *
 * Thread-safe: FlexAdapter maintains no mutable state beyond the underlying lexer.
 */
class RosettaLexerAdapter : FlexAdapter(RosettaLexer(null))
