package com.craftware.editor.ui.impl

import com.craftware.editor.ui.UIPanel
import imgui.ImGui

class ProjectFolderPanel : UIPanel("ProjectFolderPanel", "Project Files") {
    fun render() = render {
        ImGui.textDisabled("(any project files goes here)")
    }
}