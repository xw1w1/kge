package kge.editor.ui.window

import imgui.ImGui
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.ui.EditorUIPanel

class EditorProjectFilesWindow : EditorUIPanel("Files"), IRenderable {
    override fun render(delta: Float) {
        this.beginUI()
        content = {
            ImGui.text("Project files: (Empty)")
        }
        this.endUI()
    }

    override fun pushRenderCallback(cb: IRenderCallback) { }
}
