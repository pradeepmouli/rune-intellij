package dev.mouli.rune.actions

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for HelloAction.
 * For more advanced testing with IntelliJ Platform test framework,
 * see https://plugins.jetbrains.com/docs/intellij/testing-plugins.html
 */
class HelloActionTest {
    @Test
    fun `action instance is created`() {
        val action = HelloAction()
        assertNotNull(action)
    }
}
