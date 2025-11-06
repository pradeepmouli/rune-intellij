package com.github.pmouli.rune

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * File type definition for Rune DSL (.rosetta) files.
 *
 * Registers the file extension and associates it with RosettaLanguage.
 * Provides file type metadata (name, description, icon) for the IDE.
 *
 * Thread-safe singleton accessed via companion object INSTANCE.
 */
class RosettaFileType private constructor() : LanguageFileType(RosettaLanguage.INSTANCE) {
    companion object {
        @JvmStatic
        val INSTANCE = RosettaFileType()
    }

    override fun getName(): String = "Rosetta"

    override fun getDescription(): String = "Rune DSL file"

    override fun getDefaultExtension(): String = "rosetta"

    override fun getIcon(): Icon? = null // TODO: Add Rune DSL icon in resources/icons/
}
