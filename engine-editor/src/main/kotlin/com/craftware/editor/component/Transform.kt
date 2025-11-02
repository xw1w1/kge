package com.craftware.editor.component

import com.craftware.editor.standard.GameObject
import imgui.ImGui
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

class Transform : Component() {
    var position = Vector3f(0f, 0f, 0f)
    var rotation = Vector3f(0f, 0f, 0f)
    var scale = Vector3f(1f, 1f, 1f)

    fun getLocalMatrix(): Matrix4f {
        return Matrix4f()
            .translate(position)
            .rotateXYZ(rotation.x, rotation.y, rotation.z)
            .scale(scale)
    }

    fun getWorldMatrix(obj: GameObject): Matrix4f {
        val local = getLocalMatrix()

        val parent = obj.parent
        if (parent is GameObject) {
            val parentTransform = parent.get<Transform>()
            if (parentTransform != null) {
                return Matrix4f(parentTransform.getWorldMatrix(parent)).mul(local)
            }
        }

        return local
    }

    fun getWorldPosition(obj: GameObject): Vector3f {
        val worldMat = getWorldMatrix(obj)
        val out = Vector3f()
        worldMat.getTranslation(out)
        return out
    }

    fun setWorldPosition(obj: GameObject, worldPos: Vector3f) {
        val parent = obj.parent
        if (parent is GameObject) {
            val parentTransform = parent.get<Transform>()
            if (parentTransform != null) {
                val invParent = Matrix4f(parentTransform.getWorldMatrix(parent)).invert()
                val local = Vector4f(worldPos, 1f).mul(invParent)
                position.set(local.x, local.y, local.z)
                return
            }
        }

        position.set(worldPos)
    }


    override fun onInspectorGUI() {
        super.onInspectorGUI()

        val pos = floatArrayOf(position.x, position.y, position.z)
        val rot = floatArrayOf(rotation.x, rotation.y, rotation.z)
        val scl = floatArrayOf(scale.x, scale.y, scale.z)

        ImGui.textDisabled("Position")
        if (ImGui.dragFloat3("##Position", pos, 0.1f)) position.set(pos)
        ImGui.textDisabled("Rotation")
        if (ImGui.dragFloat3("##Rotation", rot, 0.1f)) rotation.set(rot)
        ImGui.textDisabled("Scale")
        if (ImGui.dragFloat3("##Scale", scl, 0.1f)) scale.set(scl)
        ImGui.separator()
    }
}
