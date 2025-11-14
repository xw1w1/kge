package kge.editor.camera

import kge.api.render.IPerspectiveViewCamera
import kge.editor.input.GLFWKeyboard
import kge.editor.input.GLFWMouse
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

class EditorCamera : IPerspectiveViewCamera {
    override var position = Vector3f(0f, 3f, 10f)
    override var rotation = Quaternionf()

    val editorCameraComponent = EditorCameraComponent(this).also {
        it.targetCamera = this
    }

    private var yaw = 0f
    private var pitch = 0f

    override val viewMatrix = Matrix4f()
    override val projectionMatrix = Matrix4f()

    var moveSpeedBase = 8f
    var sensitivity = 0.0025f
    var zoomSpeed = 2.0f
    var isRotating = false
    private var fieldOfView: Float = 60f

    private val forward = Vector3f()
    private val right = Vector3f()
    private val up = Vector3f(0f, 1f, 0f)

    fun handleInput(delta: Float, mouse: GLFWMouse, keyboard: GLFWKeyboard) {
        val rightMouseDown = mouse.isDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)

        if (rightMouseDown && !isRotating) {
            mouse.cursorDisabled = true
            isRotating = true
        } else if (!rightMouseDown && isRotating) {
            mouse.cursorDisabled = false
            isRotating = false
        }

        if (isRotating) {
            val dx = mouse.dx.toFloat()
            val dy = mouse.dy.toFloat()

            yaw -= dx * sensitivity * 180f / Math.PI.toFloat()
            pitch -= dy * sensitivity * 180f / Math.PI.toFloat()

            pitch = pitch.coerceIn(-89f, 89f)

            rotation.identity()
                .rotateY(org.joml.Math.toRadians(yaw))
                .rotateX(org.joml.Math.toRadians(pitch))
        }

        forward.set(0f, 0f, -1f).rotate(rotation)
        right.set(1f, 0f, 0f).rotate(rotation)

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
            position.add(move)
        }

        if (mouse.scroll != 0.0) {
            val zoom = (mouse.scroll * zoomSpeed).toFloat()
            position.add(Vector3f(forward).mul(zoom))
        }
    }

    override fun updateViewMatrix(): Matrix4f {
        val inverseRotation = Quaternionf(rotation).conjugate()
        return viewMatrix.identity()
            .rotate(inverseRotation)
            .translate(-position.x, -position.y, -position.z)
    }

    override fun updateProjectionMatrix(aspect: Float): Matrix4f {
        return projectionMatrix.identity()
            .perspective(org.joml.Math.toRadians(fieldOfView), aspect, zNear, zFar)
    }

    override fun getViewProjection(aspect: Float): Matrix4f {
        return Matrix4f(updateProjectionMatrix(aspect)).mul(updateViewMatrix())
    }
}