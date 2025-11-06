package kge.editor.ui

import imgui.ImGui
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import org.lwjgl.glfw.GLFW

class EditorMenuBar : IRenderable {
    override fun render(delta: Float) {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File"))  {
                if (ImGui.beginMenu("Project")) {
                    if (ImGui.menuItem("New Project")) {
                        EditorApplication.getInstance().getProjectManager()
                            .openProject("Under development")
                    }
                    ImGui.endMenu()
                }
                if (ImGui.menuItem("Exit")) {
                    GLFW.glfwSetWindowShouldClose(EditorApplication.getInstance().getWindowHandle(), true)
                }
                ImGui.endMenu()
            }

            ImGui.endMenuBar()
        }
    }

    override fun pushRenderCallback(cb: IRenderCallback) {
        // TODO() RenderPipeline.UIRenderState.pushRenderCallback(IRenderCallback.DefaultRenderCallbackImpl)
    }
}