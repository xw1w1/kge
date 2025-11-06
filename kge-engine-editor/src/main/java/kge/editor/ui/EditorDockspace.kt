package kge.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiDockNodeFlags
import imgui.flag.ImGuiWindowFlags
import kge.api.editor.imgui.UIRenderable

class EditorDockspace : UIRenderable {
    override fun beginUI() {
        val viewport = ImGui.getMainViewport()
        ImGui.setNextWindowPos(viewport.posX, viewport.posY)
        ImGui.setNextWindowSize(viewport.sizeX, viewport.sizeY)
        ImGui.setNextWindowViewport(viewport.id)

        val windowFlags = ImGuiWindowFlags.NoDocking or
                ImGuiWindowFlags.NoTitleBar or
                ImGuiWindowFlags.NoCollapse or
                ImGuiWindowFlags.NoResize or
                ImGuiWindowFlags.NoMove or
                ImGuiWindowFlags.NoBringToFrontOnFocus or
                ImGuiWindowFlags.NoNavFocus or
                ImGuiWindowFlags.MenuBar

        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowRounding, 0f)
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowBorderSize, 0f)
        ImGui.begin("EditorDockspace", windowFlags)
        ImGui.popStyleVar(2)

        val dockId = ImGui.getID("EditorDockspace")
        ImGui.dockSpace(dockId, 0f, 0f, ImGuiDockNodeFlags.PassthruCentralNode)
    }

    override fun endUI() {
        ImGui.end()
    }
}
