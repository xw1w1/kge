package kge.editor.camera

import imgui.ImGui
import kge.api.render.IPerspectiveViewCamera
import kge.editor.component.Component
import kge.editor.ui.dragndrop.EditorDragDropField
import org.joml.Vector3f

class EditorCameraComponent(private val camera: EditorCamera) : Component("Editor Camera") {
    var targetCamera: IPerspectiveViewCamera? = camera

    override fun onInspectorUI() {
        ImGui.text("Editor Camera Settings")
        ImGui.separator()

        EditorDragDropField.objectField(
            "Target Camera",
            targetCamera,
            IPerspectiveViewCamera::class,
            onChange = { targetCamera = it }
        )

        val sensitivityArr = floatArrayOf(camera.sensitivity)
        val speedArr = floatArrayOf(camera.moveSpeedBase)

        if (ImGui.dragFloat("Sensitivity", sensitivityArr, 0.0001f, 0.0001f, 0.01f))
            camera.sensitivity = sensitivityArr[0]

        if (ImGui.dragFloat("Move Speed", speedArr, 0.1f, 0.1f, 100f))
            camera.moveSpeedBase = speedArr[0]
        ImGui.separator()
        ImGui.text("Position:")
        ImGui.text("  X: %.2f  Y: %.2f  Z: %.2f".format(camera.position.x, camera.position.y, camera.position.z))

        val euler = FloatArray(3)
        camera.rotation.getEulerAnglesXYZ(Vector3f()).also {
            euler[0] = Math.toDegrees(it.x.toDouble()).toFloat()
            euler[1] = Math.toDegrees(it.y.toDouble()).toFloat()
            euler[2] = Math.toDegrees(it.z.toDouble()).toFloat()
        }

        ImGui.text("Rotation:")
        ImGui.text("  Pitch: %.2f  Yaw: %.2f  Roll: %.2f".format(euler[0], euler[1], euler[2]))
        ImGui.separator()

        ImGui.separator()
    }
}