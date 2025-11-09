package kge.editor.ui

import imgui.ImGui
import kge.api.editor.imgui.IRenderCallback
import kge.api.std.IRenderable
import kge.editor.EditorApplication
import kge.editor.GameObject
import kge.editor.component.MeshRenderer
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
                if (ImGui.beginMenu("Create")) {
                    if (ImGui.menuItem("GameObject")) {
                        val node = GameObject()
                        scene.root.addChild(node)
                        EditorApplication.getInstance().getEditorSelection().select(node)
                    }
                    if (ImGui.menuItem("Cube")) {
                        val node = GameObject()
                        val component = MeshRenderer()
                        node.addComponent(component)
                        scene.root.addChild(node)
                        EditorApplication.getInstance().getEditorSelection().select(node)
                    }
                    ImGui.endMenu()
                }
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