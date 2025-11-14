package kge.editor.ui

import imgui.ImGui
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.ui.window.CreateMenuRenderable
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
                ImGui.endMenu()
            }

            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene()
            if (scene != null) {
                CreateMenuRenderable.render()
            }

            if (ImGui.menuItem("Exit")) {
                GLFW.glfwSetWindowShouldClose(EditorApplication.getInstance().getWindowHandle(), true)
            }
            ImGui.endMenuBar()
        }
    }

    override fun pushRenderCallback(cb: IRenderCallback) {
        // TODO() RenderPipeline.UIRenderState.pushRenderCallback(IRenderCallback.DefaultRenderCallbackImpl)
    }
}