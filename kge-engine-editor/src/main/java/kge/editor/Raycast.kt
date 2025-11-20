package kge.editor

import kge.editor.component.MeshRenderer
import kge.editor.core.GameObject
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object Raycast {
    fun getMouseRay(
        mouseX: Float, mouseY: Float,
        width: Int, height: Int,
        viewMatrix: Matrix4f,
        projectionMatrix: Matrix4f
    ): Pair<Vector3f, Vector3f> {
        val ndcX = (2.0f * mouseX) / width - 1.0f
        val ndcY = 1.0f - (2.0f * mouseY) / height

        val near = Vector4f(ndcX, ndcY, -1f, 1f)
        val far  = Vector4f(ndcX, ndcY, 1f, 1f)

        val viewProj = Matrix4f(projectionMatrix).mul(viewMatrix)
        val invViewProj = viewProj.invert()

        invViewProj.transform(near)
        invViewProj.transform(far)

        if (near.w != 0f) near.div(near.w)
        if (far.w  != 0f) far.div(far.w)

        val origin = Vector3f(near.x, near.y, near.z)
        val dir = Vector3f(far.x - near.x, far.y - near.y, far.z - near.z).normalize()

        return origin to dir
    }


    fun intersectObject(rayOrigin: Vector3f, rayDir: Vector3f, obj: GameObject): Float? {
        val transform = obj.transform
        val renderer = obj.getComponent<MeshRenderer>() ?: return null

        val model = transform.getWorldMatrix()

        val localMin = renderer.mesh.boundsMin
        val localMax = renderer.mesh.boundsMax

        val corners = arrayOf(
            Vector3f(localMin.x, localMin.y, localMin.z),
            Vector3f(localMax.x, localMin.y, localMin.z),
            Vector3f(localMin.x, localMax.y, localMin.z),
            Vector3f(localMax.x, localMax.y, localMin.z),
            Vector3f(localMin.x, localMin.y, localMax.z),
            Vector3f(localMax.x, localMin.y, localMax.z),
            Vector3f(localMin.x, localMax.y, localMax.z),
            Vector3f(localMax.x, localMax.y, localMax.z)
        )

        val worldMin = Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        val worldMax = Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

        val tmp = Vector3f()
        for (c in corners) {
            model.transformPosition(c, tmp)
            worldMin.x = min(worldMin.x, tmp.x)
            worldMin.y = min(worldMin.y, tmp.y)
            worldMin.z = min(worldMin.z, tmp.z)
            worldMax.x = max(worldMax.x, tmp.x)
            worldMax.y = max(worldMax.y, tmp.y)
            worldMax.z = max(worldMax.z, tmp.z)
        }

        return intersectRayAABB(rayOrigin, rayDir, worldMin, worldMax)
    }

    fun intersectRayAABB(rayOrigin: Vector3f, rayDir: Vector3f, min: Vector3f, max: Vector3f): Float? {
        var tMin = Float.NEGATIVE_INFINITY
        var tMax = Float.POSITIVE_INFINITY

        if (abs(rayDir.x) < 1e-8f) {
            if (rayOrigin.x < min.x || rayOrigin.x > max.x) return null
        } else {
            val ood = 1.0f / rayDir.x
            var t1 = (min.x - rayOrigin.x) * ood
            var t2 = (max.x - rayOrigin.x) * ood
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = max(tMin, t1)
            tMax = min(tMax, t2)
            if (tMin > tMax) return null
        }

        if (abs(rayDir.y) < 1e-8f) {
            if (rayOrigin.y < min.y || rayOrigin.y > max.y) return null
        } else {
            val ood = 1.0f / rayDir.y
            var t1 = (min.y - rayOrigin.y) * ood
            var t2 = (max.y - rayOrigin.y) * ood
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = max(tMin, t1)
            tMax = min(tMax, t2)
            if (tMin > tMax) return null
        }

        if (abs(rayDir.z) < 1e-8f) {
            if (rayOrigin.z < min.z || rayOrigin.z > max.z) return null
        } else {
            val ood = 1.0f / rayDir.z
            var t1 = (min.z - rayOrigin.z) * ood
            var t2 = (max.z - rayOrigin.z) * ood
            if (t1 > t2) { val tmp = t1; t1 = t2; t2 = tmp }
            tMin = max(tMin, t1)
            tMax = min(tMax, t2)
            if (tMin > tMax) return null
        }

        return if (tMin >= 0f) tMin else if (tMax >= 0f) tMax else null
    }
}