package kge.editor.ui.window

import imgui.ImGui
import kge.editor.Camera
import kge.editor.EditorApplication
import kge.editor.GameObject
import kge.editor.Primitives
import kge.editor.component.CameraComponent
import kge.editor.component.MeshRenderer

object CreateMenuRenderable {
    fun render() {
        if (ImGui.beginMenu("Create")) {
            val scene = EditorApplication.getInstance().getProjectManager().getCurrentScene() ?: return

            if (ImGui.menuItem("GameObject")) {
                val node = GameObject()
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Cube")) {
                val node = GameObject("Cube")
                node.addComponent(MeshRenderer())
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Plane")) {
                val node = GameObject("Plane")
                node.addComponent(MeshRenderer(Primitives.plane()))
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Cylinder")) {
                val node = GameObject("Cylinder")
                node.addComponent(MeshRenderer(Primitives.cylinder()))
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Pyramid")) {
                val node = GameObject("Pyramid")
                node.addComponent(MeshRenderer(Primitives.pyramid()))
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Cone")) {
                val node = GameObject("Cone")
                node.addComponent(MeshRenderer(Primitives.cone()))
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Torus")) {
                val node = GameObject("Torus")
                node.addComponent(MeshRenderer(Primitives.torus()))
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            if (ImGui.menuItem("Polygon")) {
                val node = GameObject("Polygon")
                node.addComponent(MeshRenderer(Primitives.polygon()))
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }
            ImGui.separator()
            if (ImGui.menuItem("Camera")) {
                val node = Camera()
                scene.root.addChild(node)
                EditorApplication.getInstance().getEditorSelection().select(node)
            }

            ImGui.endMenu()
        }
    }
}