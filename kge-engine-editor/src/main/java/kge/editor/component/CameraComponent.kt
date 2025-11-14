package kge.editor.component

import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import kge.editor.Camera
import kge.editor.ui.EditorFont
import kge.editor.ui.EditorText
import org.joml.Math.toRadians
import org.joml.Vector3f

// 1 camera = 1 component
class CameraComponent(
    private val camera: Camera
) : Component("Camera") {
    override fun onInspectorUI() {
        val flags = ImGuiTreeNodeFlags.DefaultOpen or
                ImGuiTreeNodeFlags.FramePadding or
                ImGuiTreeNodeFlags.SpanAvailWidth

        ImGui.dummy(0f, 2f)
        ImGui.setWindowFontScale(0.9f)
        ImGui.pushFont(EditorFont.semiBold)
        val isOpen = ImGui.treeNodeEx("Camera##${System.identityHashCode(this)}", flags)
        ImGui.popFont()
        ImGui.setWindowFontScale(1f)

        if (!isOpen) return

        EditorText.info("Projection")
        if (ImGui.dragFloat("Field of View", floatArrayOf(camera.fieldOfView), 0.1f, 1f, 179f)) {
            camera.fieldOfView = camera.fieldOfView.coerceIn(1f, 179f)
        }

        if (ImGui.dragFloat("Near Clip", floatArrayOf(camera.zNear), 0.01f, 0.001f, 10f)) {
            camera.zNear = camera.zNear.coerceAtLeast(0.001f)
        }
        if (ImGui.dragFloat("Far Clip", floatArrayOf(camera.zFar), 1f, 10f, 10000f)) {
            camera.zFar = camera.zFar.coerceAtLeast(camera.zNear + 0.1f)
        }

        ImGui.separator()

        EditorText.info("Transform Override")
        val pos = floatArrayOf(camera.position.x, camera.position.y, camera.position.z)
        if (ImGui.dragFloat3("Position", pos, 0.05f)) {
            camera.position.set(pos)
        }

        val rotEuler = FloatArray(3)
        camera.rotation.getEulerAnglesXYZ(Vector3f(rotEuler))
        if (ImGui.dragFloat3("Rotation (Euler)", rotEuler, 0.5f)) {
            camera.rotation.identity()
                .rotateX(toRadians(rotEuler[0]))
                .rotateY(toRadians(rotEuler[1]))
                .rotateZ(toRadians(rotEuler[2]))
        }

        ImGui.separator()

        ImGui.treePop()
    }
}
