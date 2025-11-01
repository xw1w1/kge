package com.craftware.editor.component

import com.craftware.editor.Primitives
import com.craftware.engine.render.GLMesh
import imgui.ImGui

class MeshRenderer(glMesh: GLMesh? = null) : Component() {
    var mesh: GLMesh = glMesh ?: Primitives.cube()
    var renderMeshFlag: Boolean = true

    override fun onInspectorGUI() {
        ImGui.text("Mesh Renderer")

        if (ImGui.checkbox("Render mesh", renderMeshFlag)) {
            renderMeshFlag = !renderMeshFlag
        }
    }

    fun render() {
        if (renderMeshFlag) mesh.render()
    }
}
