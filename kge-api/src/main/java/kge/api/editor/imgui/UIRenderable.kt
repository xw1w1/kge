package kge.api.editor.imgui

@Deprecated("Bad decision")
// should be replaced with ImGuiComponent in future
interface UIRenderable {
    fun beginUI()
    fun endUI()
}