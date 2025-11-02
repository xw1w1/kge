package com.craftware.editor.ui.impl

import com.craftware.editor.EditorApp
import com.craftware.editor.Primitives
import com.craftware.editor.component.MeshRenderer
import com.craftware.editor.standard.DirectionalLight
import com.craftware.editor.standard.PointLight
import com.craftware.engine.ExceptionFactory
import imgui.ImGui

object CreateMenu {
    fun render() {
        if (ImGui.beginMenu("Create")) {
            if (ImGui.beginMenu("Object")) {
                val scene = EditorApp.getInstance().sceneNullable
                if (scene == null) {
                    ExceptionFactory.createErrorWindow("Unable to access Create", "No active scene found")
                    return
                }
                if (ImGui.menuItem("GameObject")) scene.createEmpty("GameObject")
                if (ImGui.menuItem("Cube")) scene.createCube("Cube")
                if (ImGui.menuItem("Plane")) scene.create("Plane", scene) {
                    val renderer = MeshRenderer(Primitives.plane())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Sphere")) scene.create("Sphere", scene) {
                    val renderer = MeshRenderer(Primitives.sphere())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Cylinder")) scene.create("Cylinder", scene) {
                    val renderer = MeshRenderer(Primitives.cylinder())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Pyramid")) scene.create("Pyramid", scene) {
                    val renderer = MeshRenderer(Primitives.pyramid())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Cone")) scene.create("Cone", scene) {
                    val renderer = MeshRenderer(Primitives.cone())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Torus")) scene.create("Torus", scene) {
                    val renderer = MeshRenderer(Primitives.torus())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Octagon")) scene.create("Octagon", scene) {
                    val renderer = MeshRenderer(Primitives.octagon())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("Polygon")) scene.create("Polygon", scene) {
                    val renderer = MeshRenderer(Primitives.polygon())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("DebugGrid")) scene.create("DebugGrid", scene) {
                    val renderer = MeshRenderer(Primitives.d_Grid())
                    it.addComponent(renderer)
                }
                if (ImGui.menuItem("DebugAxis")) scene.create("DebugAxis", scene) {
                    val renderer = MeshRenderer(Primitives.d_Axis())
                    it.addComponent(renderer)
                }

                ImGui.endMenu()
            }
            if (ImGui.beginMenu("Light")) {
                val scene = EditorApp.getInstance().sceneNullable
                if (scene == null) {
                    ExceptionFactory.createErrorWindow("Unable to access Create", "No active scene found")
                    return
                }
                if (ImGui.menuItem("Point light")) {
                    scene.addChild(PointLight().also {
                        it.name = "Point Light"
                    })
                }
                if (ImGui.menuItem("Directional light")) {
                    scene.addChild(DirectionalLight().also {
                        it.name = "Directional Light"
                    })
                }
                ImGui.endMenu()
            }
            ImGui.endMenu()
        }
    }
}