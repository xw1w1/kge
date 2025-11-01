package com.craftware.editor

import com.craftware.editor.component.Component
import imgui.ImGui
import org.joml.Quaternionf
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

class EditorCamera {
    val position = Vector3f(0f, 3f, 10f)
    val rotation = Vector3f(0f, 0f, 0f)

    private val _viewMatrix = Matrix4f()
    private val _projectionMatrix = Matrix4f()

    val projectionMatrix: Matrix4f
        get() = _projectionMatrix

    val viewMatrix: Matrix4f
        get() = _viewMatrix

    private val orientation = Quaternionf()
    private val forward = Vector3f()
    private val right = Vector3f()

    private var rotating = false

    var moveSpeedBase = 8f
    var mouseSensitivity = 0.0035f
    var zoomSpeed = 1.0f

    fun updateView(): Matrix4f {
        orientation.identity()
            .rotateY(rotation.y)
            .rotateX(rotation.x)

        _viewMatrix.identity()
        _viewMatrix.rotate(orientation.conjugate(Quaternionf()))
        _viewMatrix.translate(-position.x, -position.y, -position.z)
        return _viewMatrix
    }

    fun updateProjection(aspect: Float): Matrix4f {
        _projectionMatrix.identity()
        _projectionMatrix.perspective(Math.toRadians(60.0).toFloat(), aspect, 0.1f, 1000f)
        return _projectionMatrix
    }

    fun getViewProjection(aspect: Float): Matrix4f =
        Matrix4f(updateProjection(aspect)).mul(updateView())

    fun handleInput(delta: Float) {
        val mouse = EditorApp.getInstance().getMouse()
        val keys = EditorApp.getInstance().getKeyboard()
        val rightMouseDown = mouse.getKey(GLFW.GLFW_MOUSE_BUTTON_RIGHT) == true

        if (rightMouseDown && !rotating) {
            mouse.cursorDisabled = true
            rotating = true
        } else if (!rightMouseDown && rotating) {
            mouse.cursorDisabled = false
            rotating = false
        }

        if (rotating) {
            val dx = mouse.mouseDX.toFloat()
            val dy = mouse.mouseDY.toFloat()
            rotation.y -= dx * mouseSensitivity
            rotation.x -= dy * mouseSensitivity

            val limit = (Math.PI / 2.0 - 0.001).toFloat()
            rotation.x = rotation.x.coerceIn(-limit, limit)

            mouse.mouseDX = 0.0
            mouse.mouseDY = 0.0
        }

        orientation.identity()
            .rotateY(rotation.y)
            .rotateX(rotation.x)

        forward.set(0f, 0f, -1f)
        orientation.transform(forward)
        forward.normalize()

        right.set(1f, 0f, 0f)
        orientation.transform(right)
        right.normalize()

        val move = Vector3f(0f, 0f, 0f)
        val speed = moveSpeedBase * delta

        if (keys.isDown(GLFW.GLFW_KEY_W)) {
            move.add(Vector3f(forward))
        }
        if (keys.isDown(GLFW.GLFW_KEY_S)) {
            move.sub(Vector3f(forward))
        }
        if (keys.isDown(GLFW.GLFW_KEY_A)) {
            move.sub(Vector3f(right))
        }
        if (keys.isDown(GLFW.GLFW_KEY_D)) {
            move.add(Vector3f(right))
        }

        if (keys.isDown(GLFW.GLFW_KEY_SPACE)) move.add(Vector3f(0f, 1f, 0f))
        if (keys.isDown(GLFW.GLFW_KEY_LEFT_SHIFT)) move.sub(Vector3f(0f, 1f, 0f))

        if (move.lengthSquared() > 0f) {
            move.normalize()
            move.mul(speed)
            position.add(move)
        }

        if (mouse.scroll != 0.0) {
            val zoom = (mouse.scroll * zoomSpeed).toFloat()
            position.add(Vector3f(forward).mul(zoom))
            mouse.scroll = 0.0
        }
    }

    class EditorCameraTransform(private val camera: EditorCamera) : Component() {
        override fun onInspectorGUI() {
            ImGui.text("Editor Camera Transform")
            ImGui.separator()

            val pos = floatArrayOf(camera.position.x, camera.position.y, camera.position.z)
            val rotDeg = floatArrayOf(
                Math.toDegrees(camera.rotation.x.toDouble()).toFloat(),
                Math.toDegrees(camera.rotation.y.toDouble()).toFloat(),
                Math.toDegrees(camera.rotation.z.toDouble()).toFloat()
            )

            ImGui.textDisabled("Position")
            ImGui.inputFloat3("##Position", pos)

            ImGui.textDisabled("Rotation")
            ImGui.inputFloat3("##Rotation", rotDeg)
        }
    }
}
