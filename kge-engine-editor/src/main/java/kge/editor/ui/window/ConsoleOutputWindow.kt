package kge.editor.ui.window

import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.ui.EditorUIPanel

class ConsoleOutputWindow : EditorUIPanel("Console"), IRenderable {

    private val filterExample1 = ImBoolean(false)
    private val filterExample2 = ImBoolean(true)
    private val filterExample3 = ImBoolean(true)

    private val logBuffer = StringBuilder()

    override fun render(delta: Float) {
        this.beginUI()
        content = {
            ImGui.text("Filters:")
            ImGui.sameLine()
            ImGui.checkbox("Render callbacks", filterExample1)
            ImGui.sameLine()
            ImGui.checkbox("Editor warnings", filterExample2)
            ImGui.sameLine()
            ImGui.checkbox("Editor errors", filterExample3)

            ImGui.separator()

            ImGui.beginChild("ConsoleOutput", 0f, 0f, true)
            ImGui.inputTextMultiline(
                "##console",
                ImString(logBuffer.toString()),
                    ImGui.getContentRegionAvailX(),
                    ImGui.getContentRegionAvailY(),
                    ImGuiInputTextFlags.ReadOnly
                )
            ImGui.endChild()
        }
        this.endUI()
    }

    fun log(message: String) {
        logBuffer.appendLine(message)
    }

    override fun pushRenderCallback(cb: IRenderCallback) { }
}
