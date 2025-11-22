package kge.ui.toolkit

import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags

object Layout {
    fun compound(title: String, compact: Boolean = false, content: () -> Unit) {
        val flags =
            if (compact) ImGuiTreeNodeFlags.FramePadding or ImGuiTreeNodeFlags.DefaultOpen
            else ImGuiTreeNodeFlags.DefaultOpen

        val opened = ImGui.treeNodeEx(title, flags)
        if (opened) {
            ImGui.pushID(title)
            content()
            ImGui.popID()
            ImGui.treePop()
        }
    }

    fun section(content: () -> Unit) {
        UIToolkit.spacedSeparator()

        content()

        ImGui.spacing()
    }

    fun text(str: String) = ImGui.text(str)

    fun label(str: String) {
        ImGui.text(str)
        ImGui.spacing()
    }
}