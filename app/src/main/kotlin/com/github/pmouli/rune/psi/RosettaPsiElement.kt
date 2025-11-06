package com.github.pmouli.rune.psi

import com.intellij.psi.PsiElement

/**
 * Base interface for all Rune DSL (Rosetta) PSI elements.
 *
 * All PSI elements in the Rosetta language hierarchy extend this interface.
 * Provides a common type for type checking and tree navigation.
 *
 * Thread-safe: PSI elements are immutable and accessed via ReadAction.
 */
interface RosettaPsiElement : PsiElement
