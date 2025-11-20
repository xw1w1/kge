package kge.editor.core

import kge.api.render.IPerspectiveViewCamera
import kge.editor.component.CameraComponent
import org.joml.Math
import org.joml.Matrix4f
import org.joml.Quaternionf

class Camera : GameObject("Camera"), IPerspectiveViewCamera {
    val cameraComponent: CameraComponent = this.requireComponent(CameraComponent::class)

    override val viewMatrix = Matrix4f()
    override val projectionMatrix = Matrix4f()

    override var fieldOfView: Float = 60f

    override var zNear: Float = 0.1f
    override var zFar: Float = 1000f

    override fun onUpdate() {
        super.onUpdate()

        fieldOfView = this.cameraComponent.fov
        zNear = this.cameraComponent.zNear
        zFar = this.cameraComponent.zFar
    }

    override fun updateViewMatrix(): Matrix4f {
        val inverseRotation = Quaternionf(transform.rotation).conjugate()
        return viewMatrix.identity()
            .rotate(inverseRotation)
            .translate(-transform.position.x, -transform.position.y, -transform.position.z)
    }

    override fun updateProjectionMatrix(aspect: Float): Matrix4f {
        return projectionMatrix.identity()
            .perspective(Math.toRadians(fieldOfView), aspect, zNear, zFar)
    }
}