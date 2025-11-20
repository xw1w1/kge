package kge.editor.viewport

import imgui.ImGui
import kge.editor.EditorApplication
import kge.editor.core.Camera
import kge.editor.input.GLFWKeyboard
import kge.editor.input.GLFWMouse
import kge.ui.toolkit.delegates.SerializeField
import kge.ui.toolkit.delegates.objectField
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

class ViewportCamera {
    @SerializeField("Editor Camera")
    var targetCamera by objectField<Camera>(Camera().apply {
        transform.position = Vector3f(5f, 3.4f, 5f)
        transform.lookAt(0f, 0f, 0f)
    }, immutable = true, onClickLMB = {
        it?.let { obj -> EditorApplication.getInstance().getEditorSelection().select(obj) }
    })

    var moveSpeedBase = 8f
    var sensitivity = 0.0025f
    var zoomSpeed = 2.0f

    private var yaw = 0f
    private var pitch = 0f
    var isRotating = false

    fun handleInput(delta: Float, mouse: GLFWMouse, keyboard: GLFWKeyboard) {
        val cam = targetCamera ?: return
        val trans = cam.transform

        val rightMouseDown = mouse.isDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        if (rightMouseDown && !isRotating) {
            ImGui.setWindowFocus()
            mouse.cursorDisabled = true
            isRotating = true
        } else if (!rightMouseDown && isRotating) {
            mouse.cursorDisabled = false
            isRotating = false
        }

        if (isRotating) {
            yaw -= mouse.dx.toFloat() * sensitivity * 180f / Math.PI.toFloat()
            pitch -= mouse.dy.toFloat() * sensitivity * 180f / Math.PI.toFloat()
            pitch = pitch.coerceIn(-89f, 89f)

            val rotationQuat = Quaternionf()
                .rotateY(org.joml.Math.toRadians(yaw))
                .rotateX(org.joml.Math.toRadians(pitch))

            trans.rotation = rotationQuat
        }

        val forward = Vector3f(0f, 0f, -1f).rotate(trans.rotation)
        val right = Vector3f(1f, 0f, 0f).rotate(trans.rotation)
        val up = Vector3f(0f, 1f, 0f)

        val move = Vector3f()
        val speed = moveSpeedBase * delta

        if (keyboard.isDown(GLFW.GLFW_KEY_W)) move.add(forward)
        if (keyboard.isDown(GLFW.GLFW_KEY_S)) move.sub(forward)
        if (keyboard.isDown(GLFW.GLFW_KEY_A)) move.sub(right)
        if (keyboard.isDown(GLFW.GLFW_KEY_D)) move.add(right)
        if (keyboard.isDown(GLFW.GLFW_KEY_SPACE)) move.add(up)
        if (keyboard.isDown(GLFW.GLFW_KEY_LEFT_SHIFT)) move.sub(up)

        if (move.lengthSquared() > 0f) {
            move.normalize().mul(speed)
            trans.localPosition.add(move)
        }

        if (mouse.scroll != 0.0) {
            val zoom = (mouse.scroll * zoomSpeed).toFloat()
            trans.localPosition.add(Vector3f(forward).mul(zoom))
        }
    }

    fun updateViewMatrix(): Matrix4f {
        val cam = targetCamera ?: return Matrix4f().identity()
        return cam.viewMatrix.identity()
            .rotate(cam.transform.rotation.conjugate())
            .translate(-cam.transform.position.x, -cam.transform.position.y, -cam.transform.position.z)
    }

    fun updateProjectionMatrix(aspect: Float): Matrix4f {
        val cam = targetCamera ?: return Matrix4f().identity()
        return cam.projectionMatrix.identity()
            .perspective(
                org.joml.Math.toRadians((cam).fieldOfView),
                aspect,
                cam.zNear,
                cam.zFar
            )
    }

    fun getViewProjection(aspect: Float): Matrix4f {
        return Matrix4f(updateProjectionMatrix(aspect)).mul(updateViewMatrix())
    }
}