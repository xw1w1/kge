package kge.api.std

import org.joml.Vector3f
import org.joml.Vector4f

/**
 * Represents a generic light source in the scene.
 *
 * A light defines the color, intensity, and spatial attributes
 * that affect the illumination of rendered objects.
 *
 * This interface is intended for use both in the editor environment
 * and in the final built game runtime.
 *
 * Implementations may represent different light types such as
 * point lights, directional lights, or spotlights.
 *
 * @see LightType
 */
interface ILightSource {
    var type: LightType

    var color: Vector4f
    var intensity: Float

    var direction: Vector3f
    var range: Float

    var cutoffAngle: Float

    var castsShadows: Boolean

    fun getEffectiveColor(): Vector3f {
        return Vector3f(color.x * intensity, color.y * intensity, color.z * intensity)
    }
}