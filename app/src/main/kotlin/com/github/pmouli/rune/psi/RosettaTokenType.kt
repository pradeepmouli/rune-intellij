package com.github.pmouli.rune.psi

import com.intellij.psi.tree.IElementType
import com.github.pmouli.rune.RosettaLanguage

/**
 * Custom token type for Rune DSL (Rosetta).
 *
 * Used by Grammar-Kit generated parser to represent terminal symbols.
 */
class RosettaTokenType(debugName: String) : IElementType(debugName, RosettaLanguage.INSTANCE) {
    override fun toString(): String = "RosettaTokenType.${super.toString()}"
}
