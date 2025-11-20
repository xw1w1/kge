package kge.api.std

import org.joml.Quaternionf
import org.joml.Vector3f

fun Quaternionf.toVector3f(): Vector3f {
    return this.getEulerAnglesXYZ(Vector3f()).also {
        it.x = Math.toDegrees(it.x.toDouble()).toFloat()
        it.y = Math.toDegrees(it.y.toDouble()).toFloat()
        it.z = Math.toDegrees(it.z.toDouble()).toFloat()
    }
}