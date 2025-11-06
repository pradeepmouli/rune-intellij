package com.github.pmouli.rune.project

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import javax.swing.Icon

/**
 * Module type for Rune DSL projects.
 *
 * Defines the module type used by the Rune DSL project wizard.
 */
class RuneModuleType : ModuleType<RuneModuleBuilder>(ID) {
    companion object {
        private const val ID = "RUNE_MODULE_TYPE"

        fun getInstance(): RuneModuleType {
            return ModuleTypeManager.getInstance().findByID(ID) as RuneModuleType
        }
    }

    override fun createModuleBuilder(): RuneModuleBuilder = RuneModuleBuilder()

    override fun getName(): String = "Rune DSL"

    override fun getDescription(): String = "Rune DSL module for domain modeling"

    override fun getNodeIcon(isOpened: Boolean): Icon {
        // TODO: Add module icon
        return com.intellij.openapi.util.IconLoader.getIcon("/META-INF/pluginIcon.svg", javaClass)
    }
}
