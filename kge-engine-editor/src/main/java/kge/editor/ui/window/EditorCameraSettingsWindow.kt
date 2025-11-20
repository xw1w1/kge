package kge.editor.ui.window

import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.ui.EditorUIPanel

class EditorCameraSettingsWindow : EditorUIPanel("Editor Camera"), IRenderable {
    override fun render(delta: Float) {
        this.beginUI()
        content = {
            val editorCamera = EditorApplication.getInstance().getViewport().getEditorCamera()

            drawInspectorForFields(editorCamera, "Editor Camera")
            editorCamera.targetCamera?.cameraComponent?.let {
                drawInspectorForComponent(it)
            }
        }
        this.endUI()
    }

    override fun pushRenderCallback(cb: IRenderCallback) { }
}