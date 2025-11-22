package kge.ui.toolkit

import imgui.ImGui

object UIToolkit {
    fun spacedSeparator() {
        ImGui.spacing()
        ImGui.separator()
        ImGui.spacing()
    }
}