package kge.editor.render.viewport

import kge.api.editor.EditorSelectionGizmoAxis
import kge.api.std.IRenderable
import kge.editor.GameObject
import kge.editor.ResourceLoader
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.abs

class ViewportGizmoManager {
    private var selectedAxis: EditorSelectionGizmoAxis? = null
    private var highlightedAxis: EditorSelectionGizmoAxis? = null
    var isDragging = false
        private set

    private val dragStartHit = Vector3f()
    private val dragPlaneNormal = Vector3f()

    private var originalPositions: Map<GameObject, Vector3f> = mapOf()

    private val temp = Vector3f()

    private val gizmoRenderer: ViewportGizmoRenderer = ViewportGizmoRenderer()

    fun init() {
        gizmoRenderer.init(
            ResourceLoader.loadShader(
                "standard/shaders/gizmo.vert",
                "standard/shaders/gizmo.frag"
            )
        )
    }

    fun render(viewProj: Matrix4f, gizmoPos: Vector3f, cameraPos: Vector3f) {
        gizmoRenderer.render(viewProj, gizmoPos, selectedAxis ?: highlightedAxis, cameraPos)
    }

    fun handleMouse(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        selectedObjects: Set<GameObject>,
        isDown: Boolean,
        isClicked: Boolean,
        cameraPos: Vector3f
    ) {
        if (selectedObjects.isEmpty()) {
            highlightedAxis = null
            return
        }

        val center = Vector3f()
        for (obj in selectedObjects) {
            val t = obj.transform
            center.add(t.position)
        }
        center.div(selectedObjects.size.toFloat())

        val camDist = center.distance(cameraPos)
        val scale = (camDist * 0.25f)
            .coerceIn(0.2f, 2.2f)
        val axisLength = 1.0f * scale
        val headRad = 0.06f * scale
        val pickRadius = 0.02f * scale

        if (!isDragging) {
            highlightedAxis = detectHoverAxis(rayOrigin, rayDir, center, axisLength, headRad, pickRadius)
        }

        if (isClicked && highlightedAxis != null && !isDragging) {
            selectedAxis = highlightedAxis
            isDragging = true

            dragPlaneNormal.set(computePlaneNormal(selectedAxis!!, cameraPos, center))
            getRayPlaneIntersection(rayOrigin, rayDir, center, dragPlaneNormal, dragStartHit)

            originalPositions = selectedObjects.associateWith { obj ->
                val t = obj.transform
                Vector3f(t.position)
            }

            return
        }

        if (!isDown && isDragging) {
            isDragging = false
            selectedAxis = null
            return
        }

        if (isDragging && selectedAxis != null) {
            val hit = Vector3f()
            if (!getRayPlaneIntersection(rayOrigin, rayDir, center, dragPlaneNormal, hit)) {
                return
            }
            val delta = Vector3f(hit).sub(dragStartHit)

            for (obj in selectedObjects) {
                val t = obj.transform
                val orig = originalPositions[obj] ?: continue

                when (selectedAxis) {
                    EditorSelectionGizmoAxis.TRANSLATE_X, EditorSelectionGizmoAxis.TRANSLATE_Y, EditorSelectionGizmoAxis.TRANSLATE_Z -> {
                        val localAxis = when (selectedAxis) {
                            EditorSelectionGizmoAxis.TRANSLATE_X -> Vector3f(1f, 0f, 0f)
                            EditorSelectionGizmoAxis.TRANSLATE_Y -> Vector3f(0f, 1f, 0f)
                            EditorSelectionGizmoAxis.TRANSLATE_Z -> Vector3f(0f, 0f, 1f)
                            else -> Vector3f(1f, 0f, 0f)
                        }
                        val model = t.getWorldMatrix(obj)
                        val worldAxis = model.transformDirection(localAxis).normalize()
                        val amount = delta.dot(worldAxis)
                        temp.set(worldAxis).mul(amount)
                        val newWorld = Vector3f(orig).add(temp)
                        t.setWorldPosition(obj, newWorld)
                    }
                    EditorSelectionGizmoAxis.TRANSLATE_CENTER -> {
                        val newWorld = Vector3f(orig).add(delta)
                        t.setWorldPosition(obj, newWorld)
                    }
                    EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY, EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ, EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY -> {
                        val newWorld = Vector3f(orig).add(delta)
                        t.setWorldPosition(obj, newWorld)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun computePlaneNormal(axis: EditorSelectionGizmoAxis, cameraPos: Vector3f, center: Vector3f): Vector3f {
        return when (axis) {
            EditorSelectionGizmoAxis.TRANSLATE_X -> Vector3f(0f, 1f, 0f)
            EditorSelectionGizmoAxis.TRANSLATE_Y -> Vector3f(0f, 0f, 1f)
            EditorSelectionGizmoAxis.TRANSLATE_Z -> Vector3f(1f, 0f, 0f)
            EditorSelectionGizmoAxis.TRANSLATE_CENTER -> Vector3f(cameraPos).sub(center).normalize()
            EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY -> Vector3f(0f, 0f, 1f)
            EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ -> Vector3f(0f, 1f, 0f)
            EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY -> Vector3f(1f, 0f, 0f)
            else -> { throw UnsupportedOperationException() }
        }
    }

    private fun detectHoverAxis(
        rayOrigin: Vector3f,
        rayDir: Vector3f,
        origin: Vector3f,
        axisLength: Float,
        headRad: Float,
        threshold: Float
    ): EditorSelectionGizmoAxis? {
        if (intersectSphere(rayOrigin, rayDir, origin, threshold * 5f)) return EditorSelectionGizmoAxis.TRANSLATE_CENTER
        if (intersectAxisCylinder(rayOrigin, rayDir, origin,
                Vector3f(1f, 0f, 0f), axisLength, headRad, threshold)) return EditorSelectionGizmoAxis.TRANSLATE_X
        if (intersectAxisCylinder(rayOrigin, rayDir, origin,
                Vector3f(0f, 1f, 0f), axisLength, headRad, threshold)) return EditorSelectionGizmoAxis.TRANSLATE_Y
        if (intersectAxisCylinder(rayOrigin, rayDir, origin,
                Vector3f(0f, 0f, 1f), axisLength, headRad, threshold)) return EditorSelectionGizmoAxis.TRANSLATE_Z
        if (intersectPlaneHandle(rayOrigin, rayDir, origin,
                Vector3f(1f, 0f, 0f),
                Vector3f(0f, 1f, 0f), axisLength)) return EditorSelectionGizmoAxis.TRANSLATE_PLANE_XY
        if (intersectPlaneHandle(rayOrigin, rayDir, origin,
                Vector3f(1f, 0f, 0f),
                Vector3f(0f, 0f, 1f), axisLength)) return EditorSelectionGizmoAxis.TRANSLATE_PLANE_XZ
        if (intersectPlaneHandle(rayOrigin, rayDir, origin,
                Vector3f(0f, 1f, 0f),
                Vector3f(0f, 0f, 1f), axisLength)) return EditorSelectionGizmoAxis.TRANSLATE_PLANE_ZY
        return null
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
        val l = Vector3f(center).sub(rayOrigin)
        val tca = l.dot(rayDir)
        if (tca < 0f) return false
        val d2 = l.lengthSquared() - tca * tca
        return d2 <= radius * radius
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
        val along = Vector3f(ptOnAxis).sub(axisOrigin).dot(axisDir.normalize())
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
        val n = Vector3f(dirA).cross(dirB, Vector3f()).normalize()
        val hit = Vector3f()
        if (!getRayPlaneIntersection(rayOrigin, rayDir, p0, n, hit)) return false
        return pointInTriangle(hit, p0, p1, p2)
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
}