package com.craftware.editor.ui

import com.craftware.editor.EditorApp
import com.craftware.editor.Window
import imgui.ImGui
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw

class ImGuiContext(private val window: Window) {
    private val imGuiGl3 = ImGuiImplGl3()
    private val imGuiGlfw = ImGuiImplGlfw()

    fun initImGui() {
        ImGui.createContext()
        val io = ImGui.getIO()
        io.configFlags = io.configFlags or ImGuiConfigFlags.NavEnableKeyboard

        imGuiGlfw.init(window.getHandle(), true)
        imGuiGl3.init("#version 150")
    }

    fun newFrame() {
        imGuiGlfw.newFrame()
        ImGui.newFrame()
    }

    fun render() {
        ImGui.render()
        imGuiGl3.renderDrawData(ImGui.getDrawData())
        EditorApp.getInstance().getMouse().endFrame()
    }

    fun dispose() {
        imGuiGl3.dispose()
        imGuiGlfw.dispose()
        ImGui.destroyContext()
    }
}
