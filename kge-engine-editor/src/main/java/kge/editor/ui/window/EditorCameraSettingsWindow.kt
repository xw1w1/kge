package kge.editor.ui.window

import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.camera.EditorCamera
import kge.editor.ui.EditorText
import kge.editor.ui.EditorUIPanel

class EditorCameraSettingsWindow : EditorUIPanel("Editor Camera"), IRenderable {
    override fun render(delta: Float) {
        this.beginUI()
        content = {
            val editorCamera = EditorApplication.getInstance().getProjectManager().getCurrentScene()?.activeCamera

            if (editorCamera == null) {
                EditorText.header("(No editor camera)")
            } else {
                editorCamera as EditorCamera
                editorCamera.editorCameraComponent.onInspectorUI()
            }
        }
        this.endUI()
    }

    override fun pushRenderCallback(cb: IRenderCallback) { }
}