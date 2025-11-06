package com.github.pmouli.rune.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.github.pmouli.rune.psi.RosettaFile
import com.github.pmouli.rune.psi.RosettaNamedElement

/**
 * Structure view factory for Rune DSL (Rosetta) files.
 *
 * Provides the file outline/structure view showing:
 * - Namespace declaration
 * - Type declarations with attributes
 * - Function declarations
 * - Enum declarations
 *
 * Displayed in the Structure tool window (Alt+7 / Cmd+7).
 *
 * Thread-safe: PSI access occurs within ReadAction by IntelliJ Platform.
 */
class RosettaStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile) = object : TreeBasedStructureViewBuilder() {
        override fun createStructureViewModel(editor: Editor?) =
            RosettaStructureViewModel(psiFile as RosettaFile, editor)
    }
}

/**
 * Structure view model for Rosetta files.
 */
class RosettaStructureViewModel(
    psiFile: RosettaFile,
    editor: Editor?
) : TextEditorBasedStructureViewModel(editor, psiFile) {
    
    override fun getRoot() = RosettaFileStructureViewElement(psiFile as RosettaFile)

    override fun getSuitableClasses() = arrayOf(
        RosettaNamedElement::class.java
    )
}

/**
 * Structure view element for Rosetta file root.
 */
class RosettaFileStructureViewElement(
    private val file: RosettaFile
) : StructureViewTreeElement {
    
    override fun getValue() = file
    
    override fun navigate(requestFocus: Boolean) {
        file.navigate(requestFocus)
    }
    
    override fun canNavigate() = file.canNavigate()
    
    override fun canNavigateToSource() = file.canNavigateToSource()
    
    override fun getPresentation() = object : com.intellij.navigation.ItemPresentation {
        override fun getPresentableText() = file.name
        override fun getLocationString() = null
        override fun getIcon(unused: Boolean) = file.getIcon(0)
    }
    
    override fun getChildren(): Array<StructureViewTreeElement> {
        // Find all top-level named elements (types, functions, enums)
        val namedElements = PsiTreeUtil.findChildrenOfType(file, RosettaNamedElement::class.java)
        
        return namedElements
            .filter { it.parent == file } // Only direct children
            .map { RosettaNamedElementStructureViewElement(it) }
            .toTypedArray()
    }
}

/**
 * Structure view element for named elements (types, functions, enums).
 */
class RosettaNamedElementStructureViewElement(
    private val element: RosettaNamedElement
) : StructureViewTreeElement {
    
    override fun getValue() = element
    
    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }
    
    override fun canNavigate() = element.canNavigate()
    
    override fun canNavigateToSource() = element.canNavigateToSource()
    
    override fun getPresentation() = object : com.intellij.navigation.ItemPresentation {
        override fun getPresentableText() = element.name ?: "Unknown"
        
        override fun getLocationString(): String? {
            // Show containing type for nested elements
            val parent = element.parent
            if (parent is RosettaNamedElement) {
                return parent.name
            }
            return null
        }
        
        override fun getIcon(unused: Boolean) = element.getIcon(0)
    }
    
    override fun getChildren(): Array<StructureViewTreeElement> {
        // Find nested named elements (e.g., attributes inside types)
        val children = PsiTreeUtil.findChildrenOfType(element, RosettaNamedElement::class.java)
        
        return children
            .filter { it.parent == element } // Only direct children
            .map { RosettaNamedElementStructureViewElement(it) }
            .toTypedArray()
    }
}
