package dev.mouli.rune.highlight

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Factory for creating syntax highlighters for Rune DSL (Rosetta) files.
 *
 * Required by IntelliJ Platform to instantiate syntax highlighters
 * for different project contexts and file types.
 *
 * Thread-safe: Returns new highlighter instances per invocation.
 */
class RosettaSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(
        project: Project?,
        virtualFile: VirtualFile?,
    ): SyntaxHighlighter {
        return RosettaSyntaxHighlighter()
    }
}
