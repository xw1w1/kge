package kge.editor.ui

import imgui.ImGui
import imgui.flag.ImGuiDockNodeFlags

class EditorDockspace {
    private val dockspaceId: String = "EditorDockspace"
    private var dockspaceFlags = ImGuiDockNodeFlags.None

    fun setFlags(flags: Int) {
        dockspaceFlags = flags
    }

    fun begin() {
        val io = ImGui.getIO()
        io.configFlags = io.configFlags or imgui.flag.ImGuiConfigFlags.DockingEnable

        val viewport = ImGui.getMainViewport()
        ImGui.setNextWindowPos(viewport.posX, viewport.posY)
        ImGui.setNextWindowSize(viewport.sizeX, viewport.sizeY)
        ImGui.setNextWindowViewport(viewport.id)

        val windowFlags = imgui.flag.ImGuiWindowFlags.NoTitleBar or
                imgui.flag.ImGuiWindowFlags.NoCollapse or
                imgui.flag.ImGuiWindowFlags.NoResize or
                imgui.flag.ImGuiWindowFlags.NoMove or
                imgui.flag.ImGuiWindowFlags.NoBringToFrontOnFocus or
                imgui.flag.ImGuiWindowFlags.NoNavFocus

        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowRounding, 0f)
        ImGui.pushStyleVar(imgui.flag.ImGuiStyleVar.WindowBorderSize, 0f)
        ImGui.begin(dockspaceId, windowFlags)
        ImGui.popStyleVar(2)

        val dockId = ImGui.getID(dockspaceId)
        ImGui.dockSpace(dockId, 0f, 0f, dockspaceFlags)
    }

    fun end() {
        ImGui.end()
    }
}