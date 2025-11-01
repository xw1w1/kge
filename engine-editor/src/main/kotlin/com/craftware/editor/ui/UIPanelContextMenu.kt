package com.craftware.editor.ui

import imgui.ImGui

class UIPanelContextMenu(val id: String) {
    private val actions = mutableListOf<() -> Unit>()

    fun add(label: String, shortcut: String? = null, enabled: Boolean = true, onClick: () -> Unit) {
        actions += {
            if (ImGui.menuItem(label, shortcut ?: "", false, enabled)) {
                onClick()
            }
        }
    }

    fun submenu(label: String, content: UIPanelContextMenu.() -> Unit) {
        actions += {
            if (ImGui.beginMenu(label)) {
                UIPanelContextMenu(label).apply(content).renderInternal()
                ImGui.endMenu()
            }
        }
    }

    fun separator() {
        actions += { ImGui.separator() }
    }

    fun render() {
        if (ImGui.beginPopup(id)) {
            renderInternal()
            ImGui.endPopup()
        }
    }

    private fun renderInternal() {
        for (a in actions) a()
    }
}
