package dev.mouli.rune

import com.intellij.lang.Language

/**
 * Language definition for Rune DSL (Rosetta).
 *
 * Defines the language ID used throughout the IntelliJ Platform for registering
 * file types, parsers, lexers, and language-specific features.
 *
 * Thread-safe singleton accessed via companion object INSTANCE.
 */
class RosettaLanguage private constructor() : Language("Rosetta") {
    companion object {
        @JvmStatic
        val INSTANCE = RosettaLanguage()
    }

    override fun getDisplayName(): String = "Rune DSL"

    override fun isCaseSensitive(): Boolean = true
}
