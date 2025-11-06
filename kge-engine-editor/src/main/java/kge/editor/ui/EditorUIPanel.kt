package kge.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import kge.api.editor.imgui.UIRenderable

open class EditorUIPanel(var title: String) : UIRenderable {
    var flags: Int = 0
        get() = field or windowFlagsMask()
    var isResizable: Boolean = true
    var isPinned: Boolean = false

    var content: () -> Unit = {}

    var onUserZoneRightClick: () -> Unit = {} // HierarchyPanel
    var onWindowHeaderRightClick: () -> Unit = { uiPanelContextMenu.let { ImGui.openPopup(it.id) } }

    private var uiPanelContextMenu: EditorUIPanelContextMenu = EditorUIPanelContextMenu.Default(this)

    override fun beginUI() {
        ImGui.begin(title, flags)

        //handleWindowDragging()
        content()

        getIsWindowHeaderClicked()
        uiPanelContextMenu.beginUI()
        uiPanelContextMenu.endUI()
    }

    override fun endUI() {
        ImGui.end()
    }

    fun getMouseInHeader(): Boolean {
        val mouseX = ImGui.getMousePosX()
        val mouseY = ImGui.getMousePosY()
        val winX = ImGui.getWindowPosX()
        val winY = ImGui.getWindowPosY()
        val winW = ImGui.getWindowWidth()
        val headerH = ImGui.getFrameHeight()

        return mouseX in winX..(winX + winW) && mouseY in winY..(winY + headerH)
    }

    private fun getIsWindowHeaderClicked() {
        if (getMouseInHeader()) {
            if (ImGui.isMouseClicked(1)) {
                onWindowHeaderRightClick()
            }
        }
    }

    private fun windowFlagsMask(): Int {
        var flags = 0
        if (isPinned) flags = flags or ImGuiWindowFlags.NoMove
        if (!isResizable) flags = flags or ImGuiWindowFlags.NoResize
        return flags
    }
}