package com.github.pmouli.rune.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.github.pmouli.rune.RosettaFileType
import com.github.pmouli.rune.RosettaLanguage

/**
 * PSI file element for Rune DSL (.rosetta) files.
 *
 * Represents the root of the PSI tree for a Rosetta file.
 * Provides access to the file's top-level declarations (namespace, types, functions, etc.).
 *
 * Thread-safe: PSI files are accessed via ReadAction in IntelliJ Platform.
 */
class RosettaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, RosettaLanguage.INSTANCE) {
    override fun getFileType(): FileType = RosettaFileType.INSTANCE

    override fun toString(): String = "Rosetta File"
}
