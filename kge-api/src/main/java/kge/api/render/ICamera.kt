package kge.api.render

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Represents a simple camera interface providing
 * access to core camera transformation and projection data.
 *
 * This interface defines the essential properties and operations required
 * for implementing any camera in a 3D environment.
 * It exposes both spatial transformation (position, rotation)
 * and projection parameters (view and projection matrices).
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
interface ICamera {
    val viewMatrix: Matrix4f
    val projectionMatrix: Matrix4f

    fun updateViewMatrix(): Matrix4f
    fun updateProjectionMatrix(aspect: Float): Matrix4f

    fun getViewProjection(aspect: Float): Matrix4f {
        return Matrix4f(updateProjectionMatrix(aspect)).mul(updateViewMatrix())
    }

    fun onActivate() {}

    fun onDeactivate() {}
}