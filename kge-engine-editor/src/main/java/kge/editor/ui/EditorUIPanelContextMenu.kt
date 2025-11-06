package kge.editor.ui

import imgui.ImGui
import kge.api.editor.imgui.UIRenderable

open class EditorUIPanelContextMenu(val id: String) : UIRenderable {
    private val renderActions = mutableListOf<() -> Unit>()

    fun addRow(
        label: String,
        shortcut: String? = null,
        selected: () -> Boolean,
        enabled: Boolean,
        onClick: () -> Unit
    ) {
        renderActions += {
            if (ImGui.menuItem(label, shortcut ?: "", selected(), enabled)) {
                onClick()
            }
        }
    }

    override fun beginUI() {
        if (ImGui.beginPopup(id)) {
            for (action in renderActions) action()
            ImGui.endPopup()
        }
    }

    override fun endUI() { /** not used for my mental safety - xw1w1 **/ }

    class Default(
        panel: EditorUIPanel
    ) : EditorUIPanelContextMenu("DefaultEditorUIPanelContextMenu") {
        init {
            addRow("Resizable", "", selected = { panel.isResizable }, true) {
                panel.isResizable = !panel.isResizable
            }

            addRow("Pin to Dockspace", "", selected = { panel.isPinned }, true) {
                panel.isPinned = !panel.isPinned
            }
        }
    }
}