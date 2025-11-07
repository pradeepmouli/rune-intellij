package dev.mouli.rune.highlight

import dev.mouli.rune.RosettaFileType
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

/**
 * Color settings page for Rune DSL (Rosetta) in IDE preferences.
 *
 * Allows users to customize syntax highlighting colors and font styles
 * for different token categories (keywords, strings, comments, etc.).
 *
 * Thread-safe: Uses immutable descriptors and sample text.
 */
class RosettaColorSettingsPage : ColorSettingsPage {
    companion object {
        private val DESCRIPTORS =
            arrayOf(
                AttributesDescriptor("Keyword", RosettaSyntaxHighlighter.KEYWORD),
                AttributesDescriptor("String", RosettaSyntaxHighlighter.STRING),
                AttributesDescriptor("Number", RosettaSyntaxHighlighter.NUMBER),
                AttributesDescriptor("Comment", RosettaSyntaxHighlighter.COMMENT),
                AttributesDescriptor("Identifier", RosettaSyntaxHighlighter.IDENTIFIER),
                AttributesDescriptor("Operator", RosettaSyntaxHighlighter.OPERATOR),
                AttributesDescriptor("Braces", RosettaSyntaxHighlighter.BRACES),
                AttributesDescriptor("Brackets", RosettaSyntaxHighlighter.BRACKETS),
                AttributesDescriptor("Parentheses", RosettaSyntaxHighlighter.PARENTHESES),
                AttributesDescriptor("Dot", RosettaSyntaxHighlighter.DOT),
                AttributesDescriptor("Comma", RosettaSyntaxHighlighter.COMMA),
                AttributesDescriptor("Semicolon", RosettaSyntaxHighlighter.SEMICOLON),
            )

        private const val DEMO_TEXT = """
namespace com.example

// Sample type declaration
type Product:
    productId string (1..1)
    name string (1..1)
    price number (0..1)
    inStock boolean (1..1)

// Sample function declaration
func calculateTotal:
    inputs:
        products Product (0..*)
    output:
        total number (1..1)
    post-condition:
        total = products -> price -> sum

// Sample enum declaration
enum Status:
    Active, Inactive, Pending
"""
    }

    override fun getIcon(): Icon? = RosettaFileType.INSTANCE.icon

    override fun getHighlighter(): SyntaxHighlighter = RosettaSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Rune DSL"
}
