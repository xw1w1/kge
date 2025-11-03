package kge.api.std

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Represents a basic 3D transformation interface that defines
 * position, rotation, and scale in world or local space.
 *
 * Implementations of this interface can be used in any spatial
 * entity, such as a camera, light, or any other game object that exists
 * within a 3D scene.
 *
 * This abstraction provides standard access to transformation
 * components and utility methods to construct transformation matrices.
 *
 * ### Typical responsibilities
 * - Storing spatial attributes (`position`, `rotation`, `scale`)
 * - Building local/world transformation matrices
 * - Converting between coordinate spaces
 *
 * @see
 */
interface ITransform {
    var position: Vector3f
    var rotation: Quaternionf
    var scale: Vector3f

    fun getLocalMatrix(): Matrix4f {
        return Matrix4f()
            .translate(position)
            .rotate(rotation)
            .scale(scale)
    }

    fun up(): Vector3f = rotation.transform(Vector3f(0f, 1f, 0f))
    fun right(): Vector3f = rotation.transform(Vector3f(1f, 0f, 0f))
    fun forward(): Vector3f = rotation.transform(Vector3f(0f, 0f, -1f))
}