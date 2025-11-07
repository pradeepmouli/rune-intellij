package dev.mouli.rune.psi

import dev.mouli.rune.RosettaLanguage
import com.intellij.psi.tree.IElementType

/**
 * Custom element type for Rune DSL (Rosetta) PSI tree nodes.
 *
 * Used by Grammar-Kit generated parser to represent non-terminal symbols
 * in the PSI tree.
 */
class RosettaElementType(debugName: String) : IElementType(debugName, RosettaLanguage.INSTANCE) {
    override fun toString(): String = "RosettaElementType.${super.toString()}"
}
