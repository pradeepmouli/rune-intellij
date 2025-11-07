package dev.mouli.rune.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Sample action demonstrating IntelliJ Platform best practices:
 * - Kotlin idiomatic code
 * - Runs on EDT (UI thread) for simple dialogs
 * - For long operations, would use background tasks with ProgressManager
 */
class HelloAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        Messages.showMessageDialog(
            project,
            "Hello from Rune IntelliJ Plugin!",
            "Rune",
            Messages.getInformationIcon(),
        )
    }
}
