package kge.editor.viewport

import imgui.ImColor
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import kge.editor.ui.UIIcon
import kge.editor.viewport.gizmos.GizmoMode

class ViewportHotbar {
    fun init() {
        UIIcon.loadIcon("translate", "std/icons/translate_icon.png")
        UIIcon.loadIcon("rotate", "std/icons/rotate_icon.png")
        UIIcon.loadIcon("scale", "std/icons/scale_icon.png")
    }

    fun render(activeThisFrame: Boolean) {
        val windowPos = ImGui.getWindowPos()

        val buttonSize = 25f
        val margin = 7.5f
        val posX = windowPos.x + margin
        val posY = windowPos.y + if (activeThisFrame) margin * (margin / 1.5f) else margin * margin

        ImGui.setNextWindowPos(posX, posY)
        ImGui.setNextWindowSize(buttonSize + margin + (margin * 1.8f), buttonSize * 4f + (margin * 3.1f))

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(5f, 5f))
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, ImVec2(5f, 5f))
        ImGui.pushStyleColor(ImGuiCol.WindowBg, ImColor.rgba(0, 0, 0, 128))

        if (ImGui.begin("GizmoSelector",
                ImGuiWindowFlags.NoTitleBar or
                        ImGuiWindowFlags.NoResize or
                        ImGuiWindowFlags.NoMove or
                        ImGuiWindowFlags.NoScrollbar or
                        ImGuiWindowFlags.NoSavedSettings
            )) {
            ImGui.pushStyleColor(ImGuiCol.Button, ImColor.rgba(60, 60, 60, 200))
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, ImColor.rgba(80, 80, 80, 200))
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, ImColor.rgba(100, 100, 100, 200))

            if (ImGui.imageButton("##Translate", UIIcon.getIcon("translate")!!, buttonSize, buttonSize)) {
                ViewportGizmoManager.setMode(GizmoMode.Translate)
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Move")
            }

            if (ImGui.imageButton("##Rotate", UIIcon.getIcon("rotate")!!, buttonSize, buttonSize)) {
                ViewportGizmoManager.setMode(GizmoMode.Rotate)
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Rotate")
            }

            if (ImGui.imageButton("##Scale", UIIcon.getIcon("scale")!!, buttonSize, buttonSize)) {
                ViewportGizmoManager.setMode(GizmoMode.Scale)
            }
            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Resize")
            }

            ImGui.popStyleColor(3)
        }
        ImGui.end()
        ImGui.popStyleColor(1)
        ImGui.popStyleVar(2)
    }
}