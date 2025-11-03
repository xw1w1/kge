@file:JvmName("VectorUtilities")

package kge.api.std

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

operator fun Vector4f.plusAssign(other: Vector4f) {
    this.add(other.x, other.y, other.z, other.w)
}

operator fun Vector4f.minusAssign(other: Vector4f) {
    this.sub(other.x, other.y, other.z, other.w)
}

operator fun Vector3f.plusAssign(other: Vector3f) {
    this.add(other.x, other.y, other.z)
}

operator fun Vector3f.minusAssign(other: Vector3f) {
    this.sub(other.x, other.y, other.z)
}

operator fun Vector2f.plusAssign(other: Vector2f) {
    this.add(other.x, other.y)
}

operator fun Vector2f.minusAssign(other: Vector2f) {
    this.sub(other.x, other.y)
}