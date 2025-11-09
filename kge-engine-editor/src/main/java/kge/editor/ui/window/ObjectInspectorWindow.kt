package kge.editor.ui.window

import imgui.ImGui
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.ui.EditorText
import kge.editor.ui.EditorUIPanel

class ObjectInspectorWindow : EditorUIPanel("Inspector"), IRenderable {
    override fun render(delta: Float) {
        this.beginUI()
        content = {
            val selection = EditorApplication.getInstance().getEditorSelection()

            if (selection.getSelectedObjects().size > 1) {
                EditorText.header("(Multiple selection [${selection.getSelectedObjects().size}])")
            } else if (selection.getSelectedObjects().isEmpty()) {
                EditorText.header("(No selection)")
            } else {
                val selected = selection.getSelectedObjects().first()
                EditorText.header(selected.name + " (${selected.displayType})")
                ImGui.separator()

                selected.components.forEach {
                    it.onInspectorUI()
                }
            }
        }
        this.endUI()
    }

    override fun pushRenderCallback(cb: IRenderCallback) {

    }
}