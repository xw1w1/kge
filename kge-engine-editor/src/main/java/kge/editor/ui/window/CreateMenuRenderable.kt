package kge.editor.ui.window

import imgui.ImGui
import kge.editor.core.Camera
import kge.editor.EditorApplication
import kge.editor.Primitives
import kge.editor.component.MeshRenderer
import kge.editor.core.GameObject
import kge.editor.core.Light
import kge.editor.core.SkyAndSun
import kge.editor.project.Scene

object CreateMenuRenderable {
    fun render(parent: GameObject? = null) {
        if (ImGui.beginMenu("Create")) {
            val parent = parent ?: EditorApplication.getInstance().getProjectManager().getCurrentScene()?.root ?: return

            if (ImGui.menuItem("GameObject")) {
                val node = GameObject()
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Cube")) {
                val node = GameObject("Cube")
                node.addComponent<MeshRenderer>(Primitives.cube())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Plane")) {
                val node = GameObject("Plane")
                node.addComponent<MeshRenderer>(Primitives.plane())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Cylinder")) {
                val node = GameObject("Cylinder")
                node.addComponent<MeshRenderer>(Primitives.cylinder())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Pyramid")) {
                val node = GameObject("Pyramid")
                node.addComponent<MeshRenderer>(Primitives.pyramid())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Cone")) {
                val node = GameObject("Cone")
                node.addComponent<MeshRenderer>(Primitives.cone())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Torus")) {
                val node = GameObject("Torus")
                node.addComponent<MeshRenderer>(Primitives.torus())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Polygon")) {
                val node = GameObject("Polygon")
                node.addComponent<MeshRenderer>(Primitives.polygon())
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            ImGui.separator()
            if (ImGui.menuItem("Camera")) {
                val node = Camera()
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            ImGui.separator()
            if (ImGui.menuItem("Light")) {
                val node = Light()
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Sky and Sun")) {
                val node = SkyAndSun()
                parent.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }

            ImGui.endMenu()
        }
    }
}