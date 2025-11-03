@file:JvmName("TransformUtilities")

package kge.api.std

import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.min

fun ITransform.translateBy(delta: Vector3f): ITransform {
    position += delta
    return this
}

fun ITransform.rotateEuler(x: Float, y: Float, z: Float): ITransform {
    val q = Quaternionf().rotateXYZ(x, y, z)
    rotation.mul(q)
    return this
}

fun ITransform.rotateAround(axis: Vector3f, radians: Float): ITransform {
    val q = Quaternionf().fromAxisAngleRad(axis.normalize(), radians)
    rotation.mul(q)
    return this
}

fun ITransform.setPosition(x: Float, y: Float, z: Float): ITransform {
    position.set(x, y, z)
    return this
}

fun ITransform.moveForward(distance: Float): ITransform {
    val dir = rotation.transform(Vector3f(0f, 0f, -1f))
    position.add(dir.mul(distance))
    return this
}

fun ITransform.moveUp(distance: Float): ITransform {
    val dir = rotation.transform(Vector3f(0f, 1f, 0f))
    position.add(dir.mul(distance))
    return this
}

fun ITransform.setEuler(x: Float, y: Float, z: Float): ITransform {
    rotation.identity().rotateXYZ(x, y, z)
    return this
}

fun ITransform.moveTowards(step: Vector3f, towards: Vector3f): ITransform {
    val direction = Vector3f(towards).sub(position)
    if (direction.lengthSquared() < 1e-6f) return this

    direction.normalize()
    position.add(direction.mul(step))
    return this
}

fun ITransform.moveTowards(step: Float, towards: Vector3f): ITransform {
    val direction = Vector3f(towards).sub(position)
    val distance = direction.length()
    if (distance < 1e-6f) return this

    val move = min(step, distance)
    position.add(direction.normalize().mul(move))
    return this
}

fun ITransform.moveTowards(step: Vector3f, target: ITransform): ITransform =
    moveTowards(step, target.position)

fun ITransform.moveTowards(step: Float, target: ITransform): ITransform =
    moveTowards(step, target.position)

fun ITransform.moveTowards(target: ITransform, speed: Float, deltaTime: Float): ITransform {
    val step = speed * deltaTime
    return moveTowards(step, target.position)
}

fun ITransform.moveTowards(target: Vector3f, speed: Float, deltaTime: Float): ITransform {
    val step = speed * deltaTime
    return moveTowards(step, target)
}


fun ITransform.reset(): ITransform {
    position.set(0f, 0f, 0f)
    rotation.identity()
    scale.set(1f, 1f, 1f)
    return this
}