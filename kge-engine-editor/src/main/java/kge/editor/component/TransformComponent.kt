package kge.editor.component

import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import kge.api.std.INode
import kge.editor.EditorTransformImpl
import kge.editor.ui.EditorFont
import kge.editor.ui.EditorText
import org.joml.Quaternionf
import org.joml.Vector3f

class TransformComponent(
    var transform: EditorTransformImpl? = null
) : Component("Transform") {
    override fun onInspectorUI() {
        val flags = ImGuiTreeNodeFlags.DefaultOpen or
                ImGuiTreeNodeFlags.FramePadding or
                ImGuiTreeNodeFlags.SpanAvailWidth

        ImGui.dummy(0f, 2f)

        ImGui.setWindowFontScale(0.9f)
        ImGui.pushFont(EditorFont.semiBold)
        val isOpen = ImGui.treeNodeEx("Transform##${System.identityHashCode(this)}", flags)
        ImGui.popFont()
        ImGui.setWindowFontScale(1f)

        if (isOpen) {
            if (transform != null) {
                val pos = floatArrayOf(transform!!.position.x, transform!!.position.y, transform!!.position.z)
                val scl = floatArrayOf(transform!!.scale.x, transform!!.scale.y, transform!!.scale.z)

                EditorText.info("Position")
                if (ImGui.dragFloat3("##Position", pos, 0.1f)) transform!!.position.set(pos)
                val euler = Vector3f()
                transform!!.rotation.getEulerAnglesXYZ(euler)
                val eulerDeg = floatArrayOf(
                    Math.toDegrees(euler.x.toDouble()).toFloat(),
                    Math.toDegrees(euler.y.toDouble()).toFloat(),
                    Math.toDegrees(euler.z.toDouble()).toFloat()
                )

                EditorText.info("Rotation")
                if (ImGui.dragFloat3("##Rotation", eulerDeg, 0.1f)) {
                    val rx = Math.toRadians(eulerDeg[0].toDouble()).toFloat()
                    val ry = Math.toRadians(eulerDeg[1].toDouble()).toFloat()
                    val rz = Math.toRadians(eulerDeg[2].toDouble()).toFloat()
                    transform!!.rotation = Quaternionf().rotationXYZ(rx, ry, rz)
                }

                EditorText.info("Scale")
                if (ImGui.dragFloat3("##Scale", scl, 0.1f)) transform!!.scale.set(scl)
                ImGui.separator()
            }
            ImGui.treePop()
        }
    }
}