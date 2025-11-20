package kge.ui.toolkit.dragndrop

import imgui.ImGui
import imgui.flag.ImGuiCol
import kge.ui.toolkit.EditorFont

object TextDecorations {
    fun grayedText(action: () -> Unit) {
        this.pushGrayedText()
        action()
        this.popColor()
    }

    fun pushGrayedText() {
        ImGui.pushStyleColor(ImGuiCol.Text, 150, 150, 150, 215)
    }

    fun disabledText(action: () -> Unit) {
        this.pushDisabledText()
        action()
        this.popDisabledText()
    }

    fun pushDisabledText() {
        ImGui.pushFont(EditorFont.italic)
        ImGui.pushStyleColor(ImGuiCol.Text, 150, 150, 150, 150)
    }

    fun popDisabledText() {
        this.popColor()
        ImGui.popFont()
    }

    fun popColor() {
        ImGui.popStyleColor(1)
    }
}