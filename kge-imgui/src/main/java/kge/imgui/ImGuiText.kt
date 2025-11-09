package kge.imgui

import imgui.ImGui

open class ImGuiText(val string: String) : ImGuiWidget() {
    override fun render() {
        ImGui.text(string)
    }
}
