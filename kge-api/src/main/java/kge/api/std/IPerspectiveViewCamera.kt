package kge.api.std

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * Represents a perspective-based camera interface providing
 * access to core camera transformation and projection data.
 *
 * This interface defines the essential properties and operations required
 * for implementing a perspective view camera in a 3D environment.
 * It exposes both spatial transformation (position, rotation)
 * and projection parameters (view and projection matrices, clipping planes).
 *
 * Implementations of this interface are responsible for maintaining
 * camera state and producing up-to-date view and projection matrices
 * for rendering and visibility calculations.
 *
 * Typical use cases include:
 * - In-engine scene cameras and editor viewports
 * - Runtime gameplay cameras and cinematics
 *
 * ### Properties
 * [position] - Camera position in world space.
 *
 * [rotation] – Camera orientation represented as a quaternion.
 *
 * [viewMatrix] – Transformation matrix converting world space to view space.
 *
 * [projectionMatrix] – Perspective projection matrix.
 *
 * [clipPlanesRange] – Internal range of near and far clipping planes.
 *
 * [zNear] / [zFar] – Accessors for the near and far plane distances.
 *
 * ### Functions
 * [updateViewMatrix] – Recomputes and returns the current view matrix.
 *
 * [updateProjectionMatrix] – Recomputes and returns the current projection matrix using the given aspect ratio.
 *
 * [getViewProjection] – Returns the combined view-projection matrix.
 *
 * This interface can be used both within the editor and in the final runtime,
 * providing a consistent abstraction over camera data and transformation logic.
 */
interface IPerspectiveViewCamera {
    var position: Vector3f
    var rotation: Quaternionf

    val viewMatrix: Matrix4f
    val projectionMatrix: Matrix4f

    private val clipPlanesRange: Vector2f
        get() = Vector2f(0.001f, 1000f)

    var zNear get() = clipPlanesRange.x()
        set(value) { this.clipPlanesRange.set(value, zFar) }

    var zFar get() = clipPlanesRange.y()
        set(value) { this.clipPlanesRange.set(zNear, value) }

    fun updateViewMatrix(): Matrix4f
    fun updateProjectionMatrix(aspect: Float): Matrix4f

    fun getViewProjection(aspect: Float): Matrix4f {
        return Matrix4f(updateProjectionMatrix(aspect)).mul(updateViewMatrix())
    }
}