package kge.editor

import kge.api.render.IPerspectiveViewCamera
import kge.editor.component.CameraComponent
import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class Camera : GameObject("Camera"), IPerspectiveViewCamera {
    var fieldOfView: Float = 60f

    init {
        val component = CameraComponent(this)
        this.addComponent(component)
    }

    override var position: Vector3f = Vector3f(0f, 0f, 0f)
    override var rotation: Quaternionf = Quaternionf()

    override val viewMatrix = Matrix4f()
    override val projectionMatrix = Matrix4f()

    override var zNear: Float = 0.01f
    override var zFar: Float = 1000f

    override val displayType: String
        get() = "Camera"

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
}