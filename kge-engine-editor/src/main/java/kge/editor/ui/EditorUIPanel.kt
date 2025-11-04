package kge.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import kge.api.editor.imgui.UIRenderable

class EditorUIPanel(var title: String) : UIRenderable {
    var flags: Int = ImGuiWindowFlags.None
    var isResizable: Boolean = false
    var isPinned: Boolean = false

    var content: () -> Unit = {}

    var onUserZoneRightClick: () -> Unit = {} // HierarchyPanel
    var onWindowHeaderRightClick: () -> Unit = { uiPanelContextMenu.let { ImGui.openPopup(it.id) } }

    private var uiPanelContextMenu: EditorUIPanelContextMenu = EditorUIPanelContextMenu.Default(this)

    override fun beginUI() {
        val windowFlags = flags or windowFlagsMask()

        ImGui.begin(title, windowFlags)

        content()

        checkWindowHeaderContext()
        uiPanelContextMenu.beginUI()
        uiPanelContextMenu.endUI()
    }

    override fun endUI() {
        ImGui.end()
    }

    private fun windowFlagsMask(): Int {
        var flags = 0
        if (isPinned) flags = flags or ImGuiWindowFlags.NoMove
        if (!isResizable) flags = flags or ImGuiWindowFlags.NoResize
        return flags
    }

    private fun checkWindowHeaderContext() {
        val mouseX = ImGui.getMousePosX()
        val mouseY = ImGui.getMousePosY()
        val winX = ImGui.getWindowPosX()
        val winY = ImGui.getWindowPosY()
        val winW = ImGui.getWindowWidth()
        val headerH = ImGui.getFrameHeight()

        val inHeader = mouseX in winX..(winX + winW) && mouseY in winY..(winY + headerH)

        if (inHeader) {
            if (ImGui.isMouseClicked(1)) {
                onWindowHeaderRightClick()
            }
        }
    }
}