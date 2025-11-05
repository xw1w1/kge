package kge.editor.render

import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorWindow
import kge.editor.ui.EditorDockspace
import kge.editor.ui.EditorUIPanel

class EditorApplicationUI : IRenderable {
    private val imGuiGl3 = ImGuiImplGl3()
    private val imGuiGlfw = ImGuiImplGlfw()
    private val panels: MutableList<EditorUIPanel> = mutableListOf()

    private val editorDockspace = EditorDockspace()

    fun createImGuiContext(window: EditorWindow) {
        ImGui.createContext()
        val io = ImGui.getIO()
        io.configFlags = io.configFlags or ImGuiConfigFlags.NavEnableKeyboard

        imGuiGlfw.init(window.getHandle(), true)
        imGuiGl3.init("#version 150")
    }

    fun newFrame() {
        imGuiGlfw.newFrame()
        imGuiGl3.newFrame()
        ImGui.newFrame()
    }

    fun attach(panel: EditorUIPanel) {
        this.panels += panel
    }

    fun getEditorDockspace(): EditorDockspace {
        return editorDockspace
    }

    override fun render(delta: Float) {
        ImGui.render()
        imGuiGl3.renderDrawData(ImGui.getDrawData())
        pushRenderCallback(IRenderCallback.DefaultRenderCallbackImpl.SUCCESS)
    }

    override fun pushRenderCallback(cb: IRenderCallback) { /** no logging huh? **/ }
}