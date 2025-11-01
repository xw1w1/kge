package com.craftware.editor.viewport

import com.craftware.editor.ResourceLoader
import com.craftware.editor.ui.impl.ViewportSettings
import com.craftware.editor.GameObject
import com.craftware.editor.component.Transform
import org.joml.*
import kotlin.math.abs

class GizmoManager {
    private var selectedAxis: Axis? = null
    private var highlightedAxis: Axis? = null
    var isDragging = false
        private set

    private val dragStartHit = Vector3f()
    private val dragStartWorld = Vector3f()
    private val dragPlaneNormal = Vector3f()

    private val temp = Vector3f()

    fun init() {
        ViewportGizmo.init(
            ResourceLoader.loadShader(
                "standard/shaders/gizmo.vert",
                "standard/shaders/gizmo.frag"
            )
        )
    }

    fun render(viewProj: Matrix4f, obj: GameObject, cameraPos: Vector3f) {
        if (!obj.isActive) return
        val t = obj.get<Transform>() ?: return
        val worldPos = t.getWorldPosition(obj)
        ViewportGizmo.render(viewProj, worldPos, selectedAxis ?: highlightedAxis, cameraPos)
    }

    fun handleMouse(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        obj: GameObject?,
        isDown: Boolean,
        isClicked: Boolean,
        cameraPos: Vector3f
    ) {
        if (obj == null) return
        if (!obj.isActive) return
        val transform = obj.get<Transform>() ?: return
        val worldPos = transform.getWorldPosition(obj)

        // compute scale for both render and hittests
        val camDist = worldPos.distance(cameraPos)
        val scale = (camDist * ViewportSettings.camDistModifier)
            .coerceIn(ViewportSettings.coerceMinValue, ViewportSettings.coerceMaxValue)
        val axisLength = 1.0f * scale
        val headRad = 0.06f * scale
        val pickRadius = ViewportSettings.pickingAxisSize * scale

        if (!isDragging) {
            highlightedAxis = detectHoverAxis(rayOrigin, rayDir, transform, obj, axisLength, pickRadius, headRad)
        }

        if (isClicked && highlightedAxis != null && !isDragging) {
            selectedAxis = highlightedAxis
            isDragging = true
            dragStartWorld.set(worldPos)
            when (selectedAxis) {
                Axis.X -> dragPlaneNormal.set(0f, 1f, 0f)
                Axis.Y -> dragPlaneNormal.set(0f, 0f, 1f)
                Axis.Z -> dragPlaneNormal.set(1f, 0f, 0f)
                Axis.CENTER -> dragPlaneNormal.set(cameraPos).sub(worldPos).normalize()
                Axis.PLANE_XY -> dragPlaneNormal.set(0f, 0f, 1f)
                Axis.PLANE_XZ -> dragPlaneNormal.set(0f, 1f, 0f)
                Axis.PLANE_YZ -> dragPlaneNormal.set(1f, 0f, 0f)
                else -> dragPlaneNormal.set(cameraPos).sub(worldPos).normalize()
            }
            getRayPlaneIntersection(rayOrigin, rayDir, worldPos, dragPlaneNormal, dragStartHit)
            return
        }

        if (!isDown && isDragging) {
            isDragging = false
            selectedAxis = null
            return
        }

        if (isDragging && selectedAxis != null) {
            val currentHit = Vector3f()
            if (!getRayPlaneIntersection(rayOrigin, rayDir, worldPos, dragPlaneNormal, currentHit)) return
            val delta = Vector3f(currentHit).sub(dragStartHit)

            when (selectedAxis) {
                Axis.X, Axis.Y, Axis.Z -> {
                    val axisDirLocal = when (selectedAxis) {
                        Axis.X -> Vector3f(1f, 0f, 0f)
                        Axis.Y -> Vector3f(0f, 1f, 0f)
                        Axis.Z -> Vector3f(0f, 0f, 1f)
                        else -> Vector3f(1f, 0f, 0f)
                    }
                    val model = transform.getWorldMatrix(obj)
                    val worldAxis = Vector3f(axisDirLocal)
                    model.transformDirection(worldAxis)
                    worldAxis.normalize()
                    val amount = delta.dot(worldAxis)
                    temp.set(worldAxis).mul(amount)
                    val newWorld = Vector3f(dragStartWorld).add(temp)
                    transform.setWorldPosition(obj, newWorld)
                }
                Axis.CENTER -> {
                    val newWorld = Vector3f(dragStartWorld).add(delta)
                    transform.setWorldPosition(obj, newWorld)
                }
                Axis.PLANE_XY, Axis.PLANE_XZ, Axis.PLANE_YZ -> {
                    val newWorld = Vector3f(dragStartWorld).add(delta)
                    transform.setWorldPosition(obj, newWorld)
                }
                else -> {}
            }
        }
    }

    private fun detectHoverAxis(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        transform: Transform,
        obj: GameObject,
        axisLength: Float,
        threshold: Float,
        headRadius: Float
    ): Axis? {
        val pos = transform.getWorldPosition(obj)
        val model = transform.getWorldMatrix(obj)

        val xDir = Vector3f(1f, 0f, 0f); model.transformDirection(xDir); xDir.normalize()
        val yDir = Vector3f(0f, 1f, 0f); model.transformDirection(yDir); yDir.normalize()
        val zDir = Vector3f(0f, 0f, 1f); model.transformDirection(zDir); zDir.normalize()

        // central axis
        if (intersectSphere(rayOrigin, rayDir, pos, threshold * 5.0f)) return Axis.CENTER

        // axes
        if (intersectAxisCylinder(rayOrigin, rayDir, pos, xDir, axisLength, headRadius, threshold)) return Axis.X
        if (intersectAxisCylinder(rayOrigin, rayDir, pos, yDir, axisLength, headRadius, threshold)) return Axis.Y
        if (intersectAxisCylinder(rayOrigin, rayDir, pos, zDir, axisLength, headRadius, threshold)) return Axis.Z

        // plane axis
        val planeSize = axisLength * 1.0f
        if (intersectPlaneHandle(rayOrigin, rayDir, pos, xDir, yDir, planeSize)) return Axis.PLANE_XY
        if (intersectPlaneHandle(rayOrigin, rayDir, pos, xDir, zDir, planeSize)) return Axis.PLANE_XZ
        if (intersectPlaneHandle(rayOrigin, rayDir, pos, yDir, zDir, planeSize)) return Axis.PLANE_YZ

        return null
    }

    private fun intersectAxisCylinder(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        axisOrigin: Vector3f,
        axisDir: Vector3f,
        axisLength: Float,
        radius: Float,
        threshold: Float
    ): Boolean {
        val (ptOnRay, ptOnAxis) = closestPointsBetweenLines(rayOrigin, rayDir, axisOrigin, axisDir)
        val dist = ptOnRay.distance(ptOnAxis)
        if (dist > (radius + threshold)) return false
        val along = Vector3f(ptOnAxis).sub(axisOrigin).dot(Vector3f(axisDir).normalize())
        return along in 0f..axisLength
    }

    private fun intersectPlaneHandle(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        origin: Vector3f,
        dirA: Vector3f,
        dirB: Vector3f,
        size: Float
    ): Boolean {
        val p0 = Vector3f(origin)
        val p1 = Vector3f(origin).fma(size, dirA)
        val p2 = Vector3f(origin).fma(size, dirB)
        // plane normal
        val n = Vector3f(dirA).cross(dirB, Vector3f()).normalize()
        val hit = Vector3f()
        if (!getRayPlaneIntersection(rayOrigin, rayDir, p0, n, hit)) return false

        return pointInTriangle(hit, p0, p1, p2)
    }

    private fun pointInTriangle(p: Vector3f, a: Vector3f, b: Vector3f, c: Vector3f): Boolean {
        val v0 = Vector3f(c).sub(a)
        val v1 = Vector3f(b).sub(a)
        val v2 = Vector3f(p).sub(a)

        val dot00 = v0.dot(v0)
        val dot01 = v0.dot(v1)
        val dot02 = v0.dot(v2)
        val dot11 = v1.dot(v1)
        val dot12 = v1.dot(v2)

        val invDenom = 1f / (dot00 * dot11 - dot01 * dot01).coerceAtLeast(1e-8f)
        val u = (dot11 * dot02 - dot01 * dot12) * invDenom
        val v = (dot00 * dot12 - dot01 * dot02) * invDenom

        return u >= 0f && v >= 0f && u + v <= 1f
    }

    private fun closestPointsBetweenLines(p: Vector3f, u: Vector3f, q: Vector3f, v: Vector3f): Pair<Vector3f, Vector3f> {
        val w0 = Vector3f(p).sub(q)
        val a = u.dot(u)
        val b = u.dot(v)
        val c = v.dot(v)
        val d = u.dot(w0)
        val e = v.dot(w0)
        val denom = a * c - b * b
        if (abs(denom) < 1e-6f) {
            val t = if (a > 1e-6f) (-d / a) else 0f
            val ptOnRay = Vector3f(u).mul(t).add(p)
            val ptOnAxis = Vector3f(q)
            return ptOnRay to ptOnAxis
        }
        val t = (b * e - c * d) / denom
        val s = (a * e - b * d) / denom
        val ptOnRay = Vector3f(u).mul(t).add(p)
        val ptOnAxis = Vector3f(v).mul(s).add(q)
        return ptOnRay to ptOnAxis
    }

    private fun getRayPlaneIntersection(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        planePoint: Vector3f,
        planeNormal: Vector3f,
        out: Vector3f
    ): Boolean {
        val denom = rayDir.dot(planeNormal)
        if (abs(denom) < 1e-6f) return false
        val t = Vector3f(planePoint).sub(rayOrigin).dot(planeNormal) / denom
        if (t < 0f) return false
        out.set(rayOrigin).fma(t, rayDir)
        return true
    }

    private fun intersectSphere(rayOrigin: Vector3f, rayDir: Vector3f, center: Vector3f, radius: Float): Boolean {
        val L = Vector3f(center).sub(rayOrigin)
        val tca = L.dot(rayDir)
        if (tca < 0f) return false
        val d2 = L.lengthSquared() - tca * tca
        return d2 <= radius * radius
    }
}
