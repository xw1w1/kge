package kge.editor.ui

import imgui.ImFont
import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiCol
import org.joml.Vector4f

object EditorText {
    fun text(text: String, font: ImFont, color: Vector4f? = null, imColorV: ImVec4? = null) {
        ImGui.pushFont(font)
        if (color != null) {
            ImGui.pushStyleColor(ImGuiCol.Text, color.x, color.y, color.z, color.w)
        }
        ImGui.text(text)
        if (color != null) {
            ImGui.popStyleColor()
        }
        ImGui.popFont()
    }

    fun header(text: String) {
        ImGui.pushFont(EditorFont.medium)
        ImGui.text(text)
        ImGui.popFont()
    }

    fun label(text: String) {
        ImGui.pushFont(EditorFont.regular)
        ImGui.text(text)
        ImGui.popFont()
    }

    fun info(text: String) {
        ImGui.pushFont(EditorFont.regular)
        ImGui.textWrapped(text)
        ImGui.popFont()
    }

    fun colored(text: String, color: Vector4f) {
        ImGui.pushFont(EditorFont.regular)
        ImGui.pushStyleColor(ImGuiCol.Text, color.x, color.y, color.z, color.w)
        ImGui.text(text)
        ImGui.popStyleColor()
        ImGui.popFont()
    }

    fun bold(text: String) {
        ImGui.pushFont(EditorFont.bold)
        ImGui.text(text)
        ImGui.popFont()
    }
}