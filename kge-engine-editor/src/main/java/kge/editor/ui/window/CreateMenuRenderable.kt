package kge.editor.ui.window

import imgui.ImGui
import kge.editor.EditorApplication
import kge.editor.GameObject

object CreateMenuRenderable {
    fun render() {
        if (ImGui.beginMenu("Create")) {
            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return
            if (ImGui.menuItem("GameObject")) {
                val node = GameObject()
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            ImGui.endMenu()
        }
    }
}