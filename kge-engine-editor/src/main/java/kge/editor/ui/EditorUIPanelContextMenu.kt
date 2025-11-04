package kge.editor.ui

import imgui.ImGui
import kge.api.editor.imgui.UIRenderable

open class EditorUIPanelContextMenu(val id: String) : UIRenderable {
    private val renderActions = mutableListOf<() -> Unit>()

    fun addRow(
        label: String,
        shortcut: String? = null,
        selected: Boolean = false,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        renderActions += {
            if (ImGui.menuItem(label, shortcut ?: "", selected, enabled)) {
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
            val doNotResize = panel.isResizable
            val pinToDockspace = panel.isPinned
            addRow("Lock resize", selected = doNotResize) {
                panel.isResizable = !doNotResize
            }

            addRow("Pin to Dockspace", selected = pinToDockspace) {
                panel.isPinned = !pinToDockspace
            }
        }
    }
}