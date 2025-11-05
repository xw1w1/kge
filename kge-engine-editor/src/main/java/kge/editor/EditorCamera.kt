package kge.editor

import kge.api.input.GLFWKeyboard
import kge.api.input.GLFWMouse
import kge.api.render.IPerspectiveViewCamera
import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW

class EditorCamera : IPerspectiveViewCamera {
    override var position = Vector3f(0f, 3f, 10f)
    override var rotation = Quaternionf()

    override val viewMatrix = Matrix4f()
    override val projectionMatrix = Matrix4f()

    var moveSpeedBase = 8f
    var sensitivity = 0.0035f
    var zoomSpeed = 2.0f
    private var isRotating = false
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
            val eulerY = -dx * sensitivity
            val eulerX = -dy * sensitivity

            val qx = Quaternionf().rotateX(eulerX)
            val qy = Quaternionf().rotateY(eulerY)
            rotation.mul(qy).mul(qx).normalize()

            mouse.cleanup()
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
            mouse.scroll = 0.0
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
            .perspective(toRadians(fieldOfView), aspect, zNear, zFar)
    }

    override fun getViewProjection(aspect: Float): Matrix4f {
        return Matrix4f(updateProjectionMatrix(aspect)).mul(updateViewMatrix())
    }
}